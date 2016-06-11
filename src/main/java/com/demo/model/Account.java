package com.demo.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account implements Serializable {

	@JsonIgnore
	private static final long serialVersionUID = -146386294680936829L;

	private Long id;

	private String emailId;

	private String firstName;
	private String lastName;
	private String gender;
	private String ssoProvider;
	private String password;
	private String dob;
	private String phoneNumber;

	private short isActive = 1;
	private String country;
	private String createDate;
	private String profileImage;
	private String imsId;

	public Account() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSsoProvider() {
		return ssoProvider;
	}

	public void setSsoProvider(String ssoProvider) {
		this.ssoProvider = ssoProvider;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public short getIsActive() {
		return isActive;
	}

	public void setIsActive(short isActive) {
		this.isActive = isActive;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getImsId() {
		return imsId;
	}

	public void setImsId(String imsId) {
		this.imsId = imsId;
	}

	public Account(Long id, String firstName, String lastName, String gender, String dob) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.dob = dob;
	}

	public Account(Long id, String emailId, String firstName, String lastName, String gender, String ssoProvider, String password, String dob,
			String phoneNumber, short isActive, String country, String createDate, String profileImage, String imsId) {
		super();
		this.id = id;
		this.emailId = emailId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.ssoProvider = ssoProvider;
		this.password = password;
		this.dob = dob;
		this.phoneNumber = phoneNumber;
		this.isActive = isActive;
		this.country = country;
		this.createDate = createDate;
		this.profileImage = profileImage;
		this.imsId = imsId;
	}

}
