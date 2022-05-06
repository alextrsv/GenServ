package generated;

import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import java.util.List;
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

	Long id

	String name

	String lastname

	String phone

    List<User> usersList

    List<Office> officesList
}

@Accessors
@Entity
@ToString
@EqualsHashCode
class User {

	Long id

	String lastName

	String email

	List<Country> country
}

@Accessors
@Entity
@ToString
@EqualsHashCode
class Office {

	Long id

	String title

	Country country
}