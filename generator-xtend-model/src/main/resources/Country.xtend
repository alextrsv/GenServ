package generated;

import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import java.util.Collection;
import javax.persistence.Transient
import org.eclipse.xtend.lib.annotations.EqualsHashCode
import org.eclipse.xtend.lib.annotations.ToString
import javax.persistence.JoinColumn
import ru.jpoint.xtend.demo.Entity
import org.eclipse.xtend.lib.annotations.Accessors

@Accessors
@Entity
@ToString
@EqualsHashCode
class Country {
	
	@Id 
	Long id

	@Transient
	transient boolean isNew
	
	@Column(nullable = false) 
	String name

	@OneToMany(mappedBy = "country")
    Collection<User> usersList
}

@Accessors
@Entity
@ToString
@EqualsHashCode
class User {
	@Id 
	Long id
	
	@Transient
	transient boolean isNew
	
	@Column(nullable = false) 
	String lastName

	@Column(unique = true)	
	String email

	@ManyToOne
	@JoinColumn(name = "country_id")
	Country country
}