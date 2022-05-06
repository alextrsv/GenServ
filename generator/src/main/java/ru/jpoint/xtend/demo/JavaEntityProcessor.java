package ru.jpoint.xtend.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.xtend.lib.annotations.ToString;
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.ValidationContext;
import org.eclipse.xtend.lib.macro.declaration.*;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("all")
public class JavaEntityProcessor extends EntityProcessor {
	
	@Override
	public void doValidate(List<? extends ClassDeclaration> annotatedClasses, ValidationContext context) {
		super.doValidate(annotatedClasses, context);
		annotatedClasses.stream()
				.filter(declaration -> declaration.findAnnotation(context.findTypeGlobally(ToString.class)) == null)
				.forEach(declaration -> context.addWarning(declaration, "Ты не добавил аннотацию @ToString"));
	}

	@Override
	public void doValidate(final ClassDeclaration annotatedClass, final ValidationContext context) {
		super.doValidate(annotatedClass, context);
		IterableExtensions.forEach(annotatedClass.getTypeParameters(), typeParam -> {
			typeParam.getSimpleName();
		});
		for (FieldDeclaration field : annotatedClass.getDeclaredFields()) {
			if (hasAnnotation(field, context.findTypeGlobally(ManyToOne.class))
					&& hasAnnotation(field, context.findTypeGlobally(Column.class))) {
				context.addError(field, "@Column(s) not allowed on a @ManyToOne property, use @JoinColumn(s)");
			}
		}
	}

	
	private boolean hasAnnotation(FieldDeclaration field, Type findTypeGlobally) {
		return field.findAnnotation(findTypeGlobally) != null;
	}

	@Override
	public void doRegisterGlobals(final ClassDeclaration annotatedClass, final RegisterGlobalsContext context) {		
		context.registerInterface("generated.repositories." + annotatedClass.getSimpleName() + "Repository");
	    context.registerInterface("generated.services." + annotatedClass.getSimpleName() + "Service");
		context.registerClass("generated.services.Impl." + annotatedClass.getSimpleName() + "ServiceImpl");
		context.registerClass("generated.controllers." + annotatedClass.getSimpleName() + "Controller");
		context.registerClass("generated.entity.dto." + annotatedClass.getSimpleName() + "DTO");
		context.registerClass("generated.entity." + annotatedClass.getSimpleName());
	}
	
	@Override
	public void doTransform(MutableClassDeclaration entity, TransformationContext context) {
		super.doTransform(entity, context);

		entity.addAnnotation(context.newAnnotationReference(Entity.class));
		
		createRepository(entity, context);
		createService(entity, context);
		createServiceImpl(entity, context);
		createDTO(entity, context);
		createEntity(entity, context);
		createController(entity, context);
	}


	private boolean isUnique(MutableFieldDeclaration field, TransformationContext context) {
		AnnotationReference columnAnnotation = field.findAnnotation(context.findTypeGlobally(Column.class));
		return columnAnnotation != null && columnAnnotation.getBooleanValue("unique");
	}

	private boolean hasAnyJPAAnnotation(MutableFieldDeclaration field, final TransformationContext context ){

		return (hasAnnotation(field, context.findTypeGlobally(ManyToOne.class))
				|| hasAnnotation(field, context.findTypeGlobally(ManyToMany.class))
				|| hasAnnotation(field, context.findTypeGlobally(OneToMany.class))
				|| hasAnnotation(field, context.findTypeGlobally(OneToOne.class))
				|| hasAnnotation(field, context.findTypeGlobally(Column.class))
				|| hasAnnotation(field, context.findTypeGlobally(Id.class)));
	}


	private void createEntity(final MutableClassDeclaration entity, final TransformationContext context) {
		final MutableClassDeclaration entityClass = context.findClass("generated.entity." + entity.getSimpleName());
		final MutableClassDeclaration dtoClass = context.findClass("generated.entity.dto." + entity.getSimpleName() + "DTO");

		entityClass.addAnnotation(context.newAnnotationReference(Data.class));
		entityClass.addAnnotation(context.newAnnotationReference(NoArgsConstructor.class));
		entityClass.addAnnotation(context.newAnnotationReference(AllArgsConstructor.class));
		entityClass.addAnnotation(context.newAnnotationReference(Entity.class));
		entityClass.addAnnotation(context.newAnnotationReference(Table.class, annotation -> {
			annotation.set("name", entity.getSimpleName().toLowerCase()); })
		);
		TypeReference enumType = context.newTypeReference(GenerationType.class);

		TypeReference listType = context.newTypeReference(List.class);
		TypeReference collectionType = context.newTypeReference(Collection.class, context.getAnyType());


		//добавление аннотаций на сгенерированный исходник из JaxB
		entity.getDeclaredFields().forEach(field -> {

			if (  (field.getType().getSimpleName().contains("List")
					|| field.getType().getSimpleName().contains("Collection")) && !hasAnyJPAAnnotation(field, context)){

				//Вытаскиваем Тип коллекции/листа
				TypeReference dependType = field.getType().getActualTypeArguments().get(0);
				//Ищем класс этого типа
				MutableClassDeclaration dependClass = context.findClass("generated." + dependType.toString());

				//пробегаем по полям вытащенного типа и смотрим, есть ли там интересующие нас
				dependClass.getDeclaredFields().forEach(depField -> {
					if (depField.getType().getSimpleName().equals(entity.getSimpleName())){ //ManyToOne-OneToMany
						depField.getAnnotations().forEach(annotationReference -> field.removeAnnotation(annotationReference));

						depField.addAnnotation(context.newAnnotationReference(ManyToOne.class, annotation ->{
							annotation.setBooleanValue("optional", false);
						}));
						depField.addAnnotation(context.newAnnotationReference(JoinColumn.class, annotation ->{
							annotation.setStringValue("name", entityClass.getSimpleName().toLowerCase() + "_id");
						}));

						field.addAnnotation(context.newAnnotationReference(OneToMany.class, annotation ->{
							annotation.setStringValue("mappedBy", depField.getSimpleName());
						}));
					}
					else if (depField.getType().getSimpleName().contains(entity.getSimpleName())
							&& depField.getType().getSimpleName().contains("List")){

						depField.addAnnotation(context.newAnnotationReference(ManyToMany.class, annotation ->{
							annotation.setStringValue("mappedBy", field.getSimpleName());
						}));

						field.addAnnotation(context.newAnnotationReference(ManyToMany.class));
						field.addAnnotation(context.newAnnotationReference(JoinTable.class, annotation ->{
							annotation.setStringValue("name", entityClass.getSimpleName().toLowerCase() + "_to_"
							+ dependClass.getSimpleName().toLowerCase());
							annotation.setAnnotationValue("joinColumns", context.newAnnotationReference(JoinColumn.class, argAnnotation -> {
								argAnnotation.setStringValue("name", entityClass.getSimpleName().toLowerCase() + "_id");
							}));
							annotation.setAnnotationValue("inverseJoinColumns", context.newAnnotationReference(JoinColumn.class, argAnnotation -> {
								argAnnotation.setStringValue("name", dependClass.getSimpleName().toLowerCase() + "_id");
							}));
						}));
					}
				});

			}
			else if (field.getSimpleName().equals("id") || field.getSimpleName().equals("Id")
					|| field.getSimpleName().equals("ID")){
				field.addAnnotation(context.newAnnotationReference(Id.class));
			}
			else if (!hasAnnotation(field, context.findTypeGlobally(OneToMany.class))
					&& !hasAnnotation(field, context.findTypeGlobally(OneToOne.class))
					&& !hasAnnotation(field, context.findTypeGlobally(ManyToOne.class))
					&& !hasAnnotation(field, context.findTypeGlobally(ManyToMany.class))) {
				field.addAnnotation(context.newAnnotationReference(Column.class, annotation -> {
					annotation.set("name", field.getSimpleName().toLowerCase()); })
				);
			}
		});


		entity.getDeclaredFields().forEach(field -> {
			entityClass.addField(field.getSimpleName(), dtoField -> {
				dtoField.setType(field.getType());
				field.getAnnotations().forEach(annotation -> dtoField.addAnnotation(annotation));
				if (hasAnnotation(field, context.findTypeGlobally(OneToOne.class)) ||
						hasAnnotation(field, context.findTypeGlobally(OneToMany.class)) ||
						hasAnnotation(field, context.findTypeGlobally(ManyToOne.class)) ||
						hasAnnotation(field, context.findTypeGlobally(ManyToMany.class))) {
					dtoField.addAnnotation(context.newAnnotationReference(JsonIgnore.class));
				}
			});
		});

		entityClass.addConstructor(constructor -> {
			constructor.addParameter(entity.getSimpleName().toLowerCase() + "DTO", context.newTypeReference(dtoClass));

			StringConcatenationClient _client = new StringConcatenationClient() {
				@Override
				protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
					entity.getDeclaredFields().forEach(field -> {
						if (field.findAnnotation(context.findTypeGlobally(Transient.class)) == null &&
								field.findAnnotation(context.findTypeGlobally(Id.class)) == null) {
							if (field.getType().getSimpleName().contains("List") || field.getType().getSimpleName().contains("list")
									|| field.getType().getSimpleName().contains("Collection")){
								_builder.append("this." + field.getSimpleName() + " = new ArrayList<>(" + entity.getSimpleName().toLowerCase() + "DTO.get"
										+ field.getSimpleName().toUpperCase().charAt(0) + field.getSimpleName().substring(1) + "());");
								_builder.newLineIfNotEmpty();
							}else {
								_builder.append("this." + field.getSimpleName() + " = " + entity.getSimpleName().toLowerCase() + "DTO.get"
										+ field.getSimpleName().toUpperCase().charAt(0) + field.getSimpleName().substring(1) + "();");
								_builder.newLineIfNotEmpty();
							}
						}
					});
				}
			};
			constructor.setBody(_client);
		});
	}


	private void createDTO(final MutableClassDeclaration entity, final TransformationContext context) {
		final MutableClassDeclaration dtoClass = context.findClass("generated.entity.dto." + entity.getSimpleName() + "DTO");

		dtoClass.addAnnotation(context.newAnnotationReference(Data.class));

		entity.getDeclaredFields().forEach(field -> {
			dtoClass.addField(field.getSimpleName(), dtoField -> {
				dtoField.setType(field.getType());
			});
		});
	}


	private void createRepository(final MutableClassDeclaration entity, final TransformationContext context) {
		final MutableInterfaceDeclaration repositoryType = context.findInterface("generated.repositories." +entity.getSimpleName() + "Repository");
		final MutableClassDeclaration entityClass = context.findClass("generated.entity." + entity.getSimpleName());
		context.setPrimarySourceElement(repositoryType, entityClass);

		final TypeReference entityType = context.newSelfTypeReference(entityClass);
		TypeReference keyType = context.newTypeReference(Serializable.class);

		for (FieldDeclaration field: entity.getDeclaredFields()) {
			if (field.getSimpleName().contains("id"))
				keyType = field.getType();
		}

		repositoryType.addAnnotation(context.newAnnotationReference(Repository.class));

		TypeReference repositoryInterfaceTypeReference = context.newTypeReference(
				CrudRepository.class,
				entityType,
				keyType);

		repositoryType.setExtendedInterfaces(Collections.unmodifiableList(Arrays.asList(repositoryInterfaceTypeReference)));

		IterableExtensions.filter(entity.getDeclaredFields(), field -> {
			return !field.isTransient() && !"id".equals(field.getSimpleName());
		}).forEach(field -> {
			repositoryType.addMethod("findBy" + StringExtensions.toFirstUpper(field.getSimpleName()), method -> {
				method.setReturnType(isUnique(field, context) ? 
								entityType : 
								context.newTypeReference(List.class, entityType));
				method.addParameter(field.getSimpleName(), field.getType())
						.addAnnotation(context.newAnnotationReference(Param.class, annotation -> {
							annotation.set("value", field.getSimpleName());
						}));
			});
		});
	}


	private void createService(final MutableClassDeclaration entity, final TransformationContext context) {
		final MutableInterfaceDeclaration serviceType = context.findInterface("generated.services." +entity.getSimpleName() + "Service");
		final MutableClassDeclaration dtoClass = context.findClass("generated.entity.dto." + entity.getSimpleName() + "DTO");
		final MutableClassDeclaration entityClass = context.findClass("generated.entity." + entity.getSimpleName());
		final TypeReference entityType = context.newSelfTypeReference(entityClass);

		TypeReference listType = context.newTypeReference(List.class, entityType);

		serviceType.addMethod("getAll", method -> {
			method.setReturnType(context.newTypeReference(Optional.class, listType));
		});
		serviceType.addMethod("get" + entity.getSimpleName(), method -> {
			method.setReturnType(context.newTypeReference(Optional.class, entityType));
			method.addParameter("id", context.newTypeReference(Serializable.class));
		});
		serviceType.addMethod("add" + entity.getSimpleName(), method -> {
			method.setReturnType(context.newTypeReference(Optional.class, entityType));
			method.addParameter(entity.getSimpleName().toLowerCase() + "DTO", context.newTypeReference(dtoClass));
		});
	}


	private void createServiceImpl(final MutableClassDeclaration entity, final TransformationContext context) {
		final MutableInterfaceDeclaration serviceType = context.findInterface("generated.services." +entity.getSimpleName() + "Service");
		final MutableClassDeclaration serviceImplType = context.findClass("generated.services.Impl." + entity.getSimpleName() + "ServiceImpl");
		final MutableInterfaceDeclaration interfaceType = context.findInterface("generated.repositories." + entity.getSimpleName() + "Repository");
		final MutableClassDeclaration entityClass = context.findClass("generated.entity." + entity.getSimpleName());
		final MutableClassDeclaration dtoClass = context.findClass("generated.entity.dto." + entity.getSimpleName() + "DTO");

		final TypeReference entityType = context.newSelfTypeReference(entityClass);
		final TypeReference optionalType = context.newTypeReference(Optional.class, entityType);
		final TypeReference listType = context.newTypeReference(List.class, entityType);
		final TypeReference optionalListType = context.newTypeReference(Optional.class, listType);

		serviceImplType.addAnnotation(context.newAnnotationReference(Service.class));

		TypeReference implementedType = context.newTypeReference(serviceType);

		serviceImplType.setImplementedInterfaces(Collections.unmodifiableList(Arrays.asList(implementedType)));

		serviceImplType.addField(entity.getSimpleName().toLowerCase() + "Repository", field -> {
			field.setType(context.newSelfTypeReference(interfaceType));
			field.addAnnotation(context.newAnnotationReference(Autowired.class));
		});

		serviceImplType.addMethod("get" + entity.getSimpleName(), method -> {
			method.addParameter("id", context.newTypeReference(Serializable.class));
			method.addAnnotation(context.newAnnotationReference(Override.class));
			method.setReturnType(optionalType);
			createFindByIdMethodBody(method, entity);
		});

		serviceImplType.addMethod("getAll", method -> {
			method.addAnnotation(context.newAnnotationReference(Override.class));
			method.setReturnType(optionalListType);
			createGetAllMethodBody(method, entity);
		});

		serviceImplType.addMethod("add" + entity.getSimpleName(), method -> {
			method.setReturnType(context.newTypeReference(Optional.class, entityType));
			method.addParameter(entity.getSimpleName().toLowerCase() + "DTO", context.newTypeReference(dtoClass));
			createAddNewMethodBody(method, entity);
		});
	}


	private void createController(final MutableClassDeclaration entity, final TransformationContext context) {
		final MutableClassDeclaration service = context.findClass("generated.services.Impl." + entity.getSimpleName() + "ServiceImpl");
		final MutableClassDeclaration controller = context.findClass("generated.controllers." + entity.getSimpleName() + "Controller");
		final MutableClassDeclaration entityClass = context.findClass("generated.entity." + entity.getSimpleName());
		final MutableClassDeclaration dtoClass = context.findClass("generated.entity.dto." + entity.getSimpleName() + "DTO");

		final TypeReference entityType = context.newSelfTypeReference(entityClass);

		final TypeReference responseType = context.newTypeReference(ResponseEntity.class, entityType);
		final TypeReference listType = context.newTypeReference(List.class, entityType);
		final TypeReference listResponseType = context.newTypeReference(ResponseEntity.class, listType);

		controller.addAnnotation(context.newAnnotationReference(RestController.class));
		controller.addAnnotation(context.newAnnotationReference(RequestMapping.class, tr -> {
			tr.setStringValue("path", "/" + entity.getSimpleName().toLowerCase());
		}));

		controller.addField(entity.getSimpleName().toLowerCase() + "Service", field -> {
			field.setType(context.newSelfTypeReference(service));
			field.addAnnotation(context.newAnnotationReference(Autowired.class));
		});


		controller.addMethod("get" + entity.getSimpleName(), method -> {
			method.addParameter("id", context.newTypeReference(Serializable.class))
					.addAnnotation(context.newAnnotationReference(PathVariable.class));
			method.addAnnotation(context.newAnnotationReference(GetMapping.class, tr -> {
				tr.setStringValue( "path", "/{id}");
			}));
			method.setReturnType(responseType);
			createGetEntityMethodBody(method, entity);
		});

		controller.addMethod("getAll", method -> {
			method.addAnnotation(context.newAnnotationReference(GetMapping.class));
			method.setReturnType(listResponseType);
			createCONTROLLERGetAllBody(method,  entity);
		});

		controller.addMethod("addNew" + entity.getSimpleName(), method -> {
			method.addParameter(entity.getSimpleName().toLowerCase() + "DTO", context.newTypeReference(dtoClass))
					.addAnnotation(context.newAnnotationReference(RequestBody.class));
			method.addAnnotation(context.newAnnotationReference(PostMapping.class));
			method.setReturnType(responseType);
			createCONTROLLERAddNewBody(method, entity.getSimpleName().toLowerCase() + "DTO", entity);
		});
	}
}
