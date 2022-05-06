package ru.jpoint.xtend.demo

import org.eclipse.xtend.lib.annotations.AccessorsProcessor
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration


class EntityProcessor extends AbstractClassProcessor {

	def protected void createFindByIdMethodBody(MutableMethodDeclaration it, MutableClassDeclaration entity) {
		body = '''
			return «entity.simpleName.toLowerCase»Repository.findById(id);
		'''
	}


    def protected void createGetAllMethodBody(MutableMethodDeclaration it, MutableClassDeclaration entity) {

                body = '''
                       return Optional.of((List<«entity.simpleName»>) «entity.simpleName.toLowerCase»Repository.findAll());
                '''
            }


    def protected void createAddNewMethodBody(MutableMethodDeclaration it, MutableClassDeclaration entity) {

                    body = '''
                        «entity.simpleName» new«entity.simpleName.toLowerCase» = new «entity.simpleName»(«entity.simpleName.toLowerCase»DTO);
                        return Optional.ofNullable(«entity.simpleName.toLowerCase»Repository.save(new«entity.simpleName.toLowerCase»));
                    '''
                }


    def protected void createGetEntityMethodBody(MutableMethodDeclaration it, MutableClassDeclaration entity) {

        		body = '''
        			  return «entity.simpleName.toLowerCase»Service.get«entity.simpleName»(id).map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        		'''
        	}


    def protected void createCONTROLLERGetAllBody(MutableMethodDeclaration it, MutableClassDeclaration entity) {

        		body = '''
        			  return «entity.simpleName.toLowerCase»Service.getAll().map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        		'''
        	}


    def protected void createCONTROLLERAddNewBody(MutableMethodDeclaration it, String parameterName, MutableClassDeclaration entity) {

            		body = '''
            			  return «entity.simpleName.toLowerCase»Service.add«entity.simpleName»(«parameterName»).map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            		'''
            	}

}
