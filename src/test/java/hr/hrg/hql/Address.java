package hr.hrg.hql;

import jakarta.persistence.Entity;

@Entity(name="TheAddress")
public class Address {
    Long id;
    String city;
    String street;
    Integer houseNo;
    
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public Integer getHouseNo() {
        return houseNo;
    }
    public void setHouseNo(Integer houseNo) {
        this.houseNo = houseNo;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String name) {
        this.city = name;
    }
    
    @Override
    public String toString() {
        return "Address [id=" + getId() + ", city=" + getCity() + ", street=" + getStreet() + ", houseNo=" + getHouseNo() + "]";
    }
}
