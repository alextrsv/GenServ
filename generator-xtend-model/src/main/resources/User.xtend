package generated;

//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlType;


import java.util.List;
import org.eclipse.xtend.lib.annotations.EqualsHashCode
import org.eclipse.xtend.lib.annotations.ToString
import javax.persistence.JoinColumn
import ru.jpoint.xtend.demo.Entity
import org.eclipse.xtend.lib.annotations.Accessors


@Entity
public class User {

    protected long id;
    
    protected String lastName;
    
    protected String email;
    
    protected Country country;

}