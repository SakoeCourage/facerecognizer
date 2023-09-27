package application;

import java.awt.image.BufferedImage;

public class Person {
	 private String firstName;
	    private String ID;
	    private String lastName;
	    private String employeeID;
	    private int age;
	    private String employeeNumber;
	    private String gender;
	    private String email;
	    private String fullName;
	    private String title;
	    private BufferedImage image;

    public Person(String ID,String fullName) {
    	this.ID = ID;
        this.fullName = fullName;
    }
    
    public Person(String ID,String employeeID, String fullName, BufferedImage image,String gender) {
    	this.ID = ID;
        this.fullName = fullName;
        this.employeeID = employeeID;
        this.image = image;
        this.gender = gender;
    }


    public Person() {}



    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
	public String getEmployeeNumber() {
		return employeeNumber;
	}
	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public BufferedImage getImage() {
		return image;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}
}
