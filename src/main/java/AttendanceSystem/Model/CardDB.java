package AttendanceSystem.Model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDB {

    @JsonProperty
    public String ID;
    @JsonProperty
    public String CardNo;
    public String SiteCode;
    public String FacilityCode;
    public String PinCardNo;
    public String AccessLevel;
    public String LiftAccessLevel;
    public String Name;
    public String FirstName;
    public String LastName;
    public String Title;
    public String TitleID;
    public String Position;
    public String PositionID;
    public String StaffNo;
    public String Department;
    public String DepartmentID;
    public String CardType;
    public String CardTypeID;
    public String Gender;
    public String Race;
    public String RaceID;
    public String MaritalStatus;
    public String Email;
    public Timestamp DOB;
    public Timestamp JoiningDate;
    public Timestamp ResignDate;
    public String Address;
    public String Address2;
    public String City;
    public String Region;
    public String PostalCode;
    public String State;
    public String Country;
    public String MobileNo;
    public String HomePhone;
    public String Extension;
    public String FaxNo;
    public Byte Photo;
    public String Notes;
    public String Company;
    public String CompanyID;
    public String Nric;
    public String Passport;
    public String EPF;
    public String Socco;
    public String EmployeeSystemID;
    public String TitleOfCourtesy;
    public String AlarmCard;
    public Byte ReportMode;
    public String VehicleNo;
    public String FloorNo;
    public String UnitNo;
    public String ParkingNo;
    public String Remark;
    public String EmergencyContactNo;
    public String EmergencyContactPerson;
    @JsonIgnore
    public String CardDB_01;
    public String CardDB_02;
    public String CardDB_03;
    public String CardDB_04;
    public String CardDB_05;
    public String CardDB_06;
    public String CardDB_07;
    public String CardDB_08;
    public String CardDB_09;
    public String CardDB_10;
    public String CardDB_11;
    public String CardDB_12;
    public String CardDB_13;
    public String CardDB_14;
    public String CardDB_15;
    public String CardDB_16;
    public String CardDB_17;
    public String CardDB_18;
    public String CardDB_19;
    public String CardDB_20;
    public String LiftID;
    public Byte GTCard;
    public Byte BypassAP;
    public Byte Status;
    public String TempCardNo;
    public Timestamp TempCardExpiredDate;
    public Byte NonExpired;
    public Timestamp ExpiryDate;
    public String DualCardGroup;
    public String RackAccessLevel;
    public String FPMode;
    public String Custom1;
    public String Custom2;
    public String Custom3;
    public String Custom4;
    public String Custom5;
    public Timestamp CreatedDate;
    public Timestamp LastUpdate;
    public String LastUpdateBy;
    public String LastLocation;
    public Timestamp LastLocationTime;
    public String LastTrCode;
    public Byte Del_State;
    public Timestamp Del_State_Date;
    public String CarParkGroup;
    public String CanteenTZ;
    public String AdvanceAL;
    public int UserID;
    public String FaceAccessLevel;
    public Byte Photo2;
    public Byte Photo3;
    public String Block;
    public Byte isUpdate;

    public CardDB() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCardNo() {
        return CardNo;
    }

    public void setCardNo(String cardNo) {
        CardNo = cardNo;
    }

    public String getSiteCode() {
        return SiteCode;
    }

    public void setSiteCode(String siteCode) {
        SiteCode = siteCode;
    }

    public String getFacilityCode() {
        return FacilityCode;
    }

    public void setFacilityCode(String facilityCode) {
        FacilityCode = facilityCode;
    }

    public String getPinCardNo() {
        return PinCardNo;
    }

    public void setPinCardNo(String pinCardNo) {
        PinCardNo = pinCardNo;
    }

    public String getAccessLevel() {
        return AccessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        AccessLevel = accessLevel;
    }

    public String getLiftAccessLevel() {
        return LiftAccessLevel;
    }

    public void setLiftAccessLevel(String liftAccessLevel) {
        LiftAccessLevel = liftAccessLevel;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitleID() {
        return TitleID;
    }

    public void setTitleID(String titleID) {
        TitleID = titleID;
    }

    public String getPosition() {
        return Position;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public String getPositionID() {
        return PositionID;
    }

    public void setPositionID(String positionID) {
        PositionID = positionID;
    }

    public String getStaffNo() {
        return StaffNo;
    }

    public void setStaffNo(String staffNo) {
        StaffNo = staffNo;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
    }

    public String getDepartmentID() {
        return DepartmentID;
    }

    public void setDepartmentID(String departmentID) {
        DepartmentID = departmentID;
    }

    public String getCardType() {
        return CardType;
    }

    public void setCardType(String cardType) {
        CardType = cardType;
    }

    public String getCardTypeID() {
        return CardTypeID;
    }

    public void setCardTypeID(String cardTypeID) {
        CardTypeID = cardTypeID;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getRace() {
        return Race;
    }

    public void setRace(String race) {
        Race = race;
    }

    public String getRaceID() {
        return RaceID;
    }

    public void setRaceID(String raceID) {
        RaceID = raceID;
    }

    public String getMaritalStatus() {
        return MaritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        MaritalStatus = maritalStatus;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public Timestamp getDOB() {
        return DOB;
    }

    public void setDOB(Timestamp DOB) {
        this.DOB = DOB;
    }

    public Timestamp getJoiningDate() {
        return JoiningDate;
    }

    public void setJoiningDate(Timestamp joiningDate) {
        JoiningDate = joiningDate;
    }

    public Timestamp getResignDate() {
        return ResignDate;
    }

    public void setResignDate(Timestamp resignDate) {
        ResignDate = resignDate;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getAddress2() {
        return Address2;
    }

    public void setAddress2(String address2) {
        Address2 = address2;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public void setPostalCode(String postalCode) {
        PostalCode = postalCode;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public String getHomePhone() {
        return HomePhone;
    }

    public void setHomePhone(String homePhone) {
        HomePhone = homePhone;
    }

    public String getExtension() {
        return Extension;
    }

    public void setExtension(String extension) {
        Extension = extension;
    }

    public String getFaxNo() {
        return FaxNo;
    }

    public void setFaxNo(String faxNo) {
        FaxNo = faxNo;
    }

    public Byte getPhoto() {
        return Photo;
    }

    public void setPhoto(Byte photo) {
        Photo = photo;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getCompanyID() {
        return CompanyID;
    }

    public void setCompanyID(String companyID) {
        CompanyID = companyID;
    }

    public String getNric() {
        return Nric;
    }

    public void setNric(String nric) {
        Nric = nric;
    }

    public String getPassport() {
        return Passport;
    }

    public void setPassport(String passport) {
        Passport = passport;
    }

    public String getEPF() {
        return EPF;
    }

    public void setEPF(String EPF) {
        this.EPF = EPF;
    }

    public String getSocco() {
        return Socco;
    }

    public void setSocco(String socco) {
        Socco = socco;
    }

    public String getEmployeeSystemID() {
        return EmployeeSystemID;
    }

    public void setEmployeeSystemID(String employeeSystemID) {
        EmployeeSystemID = employeeSystemID;
    }

    public String getTitleOfCourtesy() {
        return TitleOfCourtesy;
    }

    public void setTitleOfCourtesy(String titleOfCourtesy) {
        TitleOfCourtesy = titleOfCourtesy;
    }

    public String getAlarmCard() {
        return AlarmCard;
    }

    public void setAlarmCard(String alarmCard) {
        AlarmCard = alarmCard;
    }

    public Byte getReportMode() {
        return ReportMode;
    }

    public void setReportMode(Byte reportMode) {
        ReportMode = reportMode;
    }

    public String getVehicleNo() {
        return VehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        VehicleNo = vehicleNo;
    }

    public String getFloorNo() {
        return FloorNo;
    }

    public void setFloorNo(String floorNo) {
        FloorNo = floorNo;
    }

    public String getUnitNo() {
        return UnitNo;
    }

    public void setUnitNo(String unitNo) {
        UnitNo = unitNo;
    }

    public String getParkingNo() {
        return ParkingNo;
    }

    public void setParkingNo(String parkingNo) {
        ParkingNo = parkingNo;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getEmergencyContactNo() {
        return EmergencyContactNo;
    }

    public void setEmergencyContactNo(String emergencyContactNo) {
        EmergencyContactNo = emergencyContactNo;
    }

    public String getEmergencyContactPerson() {
        return EmergencyContactPerson;
    }

    public void setEmergencyContactPerson(String emergencyContactPerson) {
        EmergencyContactPerson = emergencyContactPerson;
    }

    @JsonIgnore
    public String getCardDB_01() {
        return CardDB_01;
    }

    @JsonIgnore
    public void setCardDB_01(String cardDB_01) {
        CardDB_01 = cardDB_01;
    }

    public String getCardDB_02() {
        return CardDB_02;
    }

    public void setCardDB_02(String cardDB_02) {
        CardDB_02 = cardDB_02;
    }

    public String getCardDB_03() {
        return CardDB_03;
    }

    public void setCardDB_03(String cardDB_03) {
        CardDB_03 = cardDB_03;
    }

    public String getCardDB_04() {
        return CardDB_04;
    }

    public void setCardDB_04(String cardDB_04) {
        CardDB_04 = cardDB_04;
    }

    public String getCardDB_05() {
        return CardDB_05;
    }

    public void setCardDB_05(String cardDB_05) {
        CardDB_05 = cardDB_05;
    }

    public String getCardDB_06() {
        return CardDB_06;
    }

    public void setCardDB_06(String cardDB_06) {
        CardDB_06 = cardDB_06;
    }

    public String getCardDB_07() {
        return CardDB_07;
    }

    public void setCardDB_07(String cardDB_07) {
        CardDB_07 = cardDB_07;
    }

    public String getCardDB_08() {
        return CardDB_08;
    }

    public void setCardDB_08(String cardDB_08) {
        CardDB_08 = cardDB_08;
    }

    public String getCardDB_09() {
        return CardDB_09;
    }

    public void setCardDB_09(String cardDB_09) {
        CardDB_09 = cardDB_09;
    }

    public String getCardDB_10() {
        return CardDB_10;
    }

    public void setCardDB_10(String cardDB_10) {
        CardDB_10 = cardDB_10;
    }

    public String getCardDB_11() {
        return CardDB_11;
    }

    public void setCardDB_11(String cardDB_11) {
        CardDB_11 = cardDB_11;
    }

    public String getCardDB_12() {
        return CardDB_12;
    }

    public void setCardDB_12(String cardDB_12) {
        CardDB_12 = cardDB_12;
    }

    public String getCardDB_13() {
        return CardDB_13;
    }

    public void setCardDB_13(String cardDB_13) {
        CardDB_13 = cardDB_13;
    }

    public String getCardDB_14() {
        return CardDB_14;
    }

    public void setCardDB_14(String cardDB_14) {
        CardDB_14 = cardDB_14;
    }

    public String getCardDB_15() {
        return CardDB_15;
    }

    public void setCardDB_15(String cardDB_15) {
        CardDB_15 = cardDB_15;
    }

    public String getCardDB_16() {
        return CardDB_16;
    }

    public void setCardDB_16(String cardDB_16) {
        CardDB_16 = cardDB_16;
    }

    public String getCardDB_17() {
        return CardDB_17;
    }

    public void setCardDB_17(String cardDB_17) {
        CardDB_17 = cardDB_17;
    }

    public String getCardDB_18() {
        return CardDB_18;
    }

    public void setCardDB_18(String cardDB_18) {
        CardDB_18 = cardDB_18;
    }

    public String getCardDB_19() {
        return CardDB_19;
    }

    public void setCardDB_19(String cardDB_19) {
        CardDB_19 = cardDB_19;
    }

    public String getCardDB_20() {
        return CardDB_20;
    }

    public void setCardDB_20(String cardDB_20) {
        CardDB_20 = cardDB_20;
    }

    public String getLiftID() {
        return LiftID;
    }

    public void setLiftID(String liftID) {
        LiftID = liftID;
    }

    public Byte getGTCard() {
        return GTCard;
    }

    public void setGTCard(Byte GTCard) {
        this.GTCard = GTCard;
    }

    public Byte getBypassAP() {
        return BypassAP;
    }

    public void setBypassAP(Byte bypassAP) {
        BypassAP = bypassAP;
    }

    public Byte getStatus() {
        return Status;
    }

    public void setStatus(Byte status) {
        Status = status;
    }

    public String getTempCardNo() {
        return TempCardNo;
    }

    public void setTempCardNo(String tempCardNo) {
        TempCardNo = tempCardNo;
    }

    public Timestamp getTempCardExpiredDate() {
        return TempCardExpiredDate;
    }

    public void setTempCardExpiredDate(Timestamp tempCardExpiredDate) {
        TempCardExpiredDate = tempCardExpiredDate;
    }

    public Byte getNonExpired() {
        return NonExpired;
    }

    public void setNonExpired(Byte nonExpired) {
        NonExpired = nonExpired;
    }

    public Timestamp getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        ExpiryDate = expiryDate;
    }

    public String getDualCardGroup() {
        return DualCardGroup;
    }

    public void setDualCardGroup(String dualCardGroup) {
        DualCardGroup = dualCardGroup;
    }

    public String getRackAccessLevel() {
        return RackAccessLevel;
    }

    public void setRackAccessLevel(String rackAccessLevel) {
        RackAccessLevel = rackAccessLevel;
    }

    public String getFPMode() {
        return FPMode;
    }

    public void setFPMode(String FPMode) {
        this.FPMode = FPMode;
    }

    public String getCustom1() {
        return Custom1;
    }

    public void setCustom1(String custom1) {
        Custom1 = custom1;
    }

    public String getCustom2() {
        return Custom2;
    }

    public void setCustom2(String custom2) {
        Custom2 = custom2;
    }

    public String getCustom3() {
        return Custom3;
    }

    public void setCustom3(String custom3) {
        Custom3 = custom3;
    }

    public String getCustom4() {
        return Custom4;
    }

    public void setCustom4(String custom4) {
        Custom4 = custom4;
    }

    public String getCustom5() {
        return Custom5;
    }

    public void setCustom5(String custom5) {
        Custom5 = custom5;
    }

    public Timestamp getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        CreatedDate = createdDate;
    }

    public Timestamp getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public String getLastUpdateBy() {
        return LastUpdateBy;
    }

    public void setLastUpdateBy(String lastUpdateBy) {
        LastUpdateBy = lastUpdateBy;
    }

    public String getLastLocation() {
        return LastLocation;
    }

    public void setLastLocation(String lastLocation) {
        LastLocation = lastLocation;
    }

    public Timestamp getLastLocationTime() {
        return LastLocationTime;
    }

    public void setLastLocationTime(Timestamp lastLocationTime) {
        LastLocationTime = lastLocationTime;
    }

    public String getLastTrCode() {
        return LastTrCode;
    }

    public void setLastTrCode(String lastTrCode) {
        LastTrCode = lastTrCode;
    }

    public Byte getDel_State() {
        return Del_State;
    }

    public void setDel_State(Byte del_State) {
        Del_State = del_State;
    }

    public Timestamp getDel_State_Date() {
        return Del_State_Date;
    }

    public void setDel_State_Date(Timestamp del_State_Date) {
        Del_State_Date = del_State_Date;
    }

    public String getCarParkGroup() {
        return CarParkGroup;
    }

    public void setCarParkGroup(String carParkGroup) {
        CarParkGroup = carParkGroup;
    }

    public String getCanteenTZ() {
        return CanteenTZ;
    }

    public void setCanteenTZ(String canteenTZ) {
        CanteenTZ = canteenTZ;
    }

    public String getAdvanceAL() {
        return AdvanceAL;
    }

    public void setAdvanceAL(String advanceAL) {
        AdvanceAL = advanceAL;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getFaceAccessLevel() {
        return FaceAccessLevel;
    }

    public void setFaceAccessLevel(String faceAccessLevel) {
        FaceAccessLevel = faceAccessLevel;
    }

    public Byte getPhoto2() {
        return Photo2;
    }

    public void setPhoto2(Byte photo2) {
        Photo2 = photo2;
    }

    public Byte getPhoto3() {
        return Photo3;
    }

    public void setPhoto3(Byte photo3) {
        Photo3 = photo3;
    }

    public String getBlock() {
        return Block;
    }

    public void setBlock(String block) {
        Block = block;
    }

    public Byte getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(Byte isUpdate) {
        this.isUpdate = isUpdate;
    }
}
