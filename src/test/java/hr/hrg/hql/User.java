package hr.hrg.hql;

import jakarta.persistence.Entity;

@Entity
public class User {
    Long id;
    String name;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User [id=" + getId() + ", name=" + getName() + "]";
    }
    
}
