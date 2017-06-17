package com.java.bankmanagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BankDepositAccountOrganizer {


public static void main(String[] args){
	
	String filepath="E:/workspaces/accountdetails.txt";
	try {
		System.out.println(processBankDepositData(filepath));
	} catch (BankOrganizerException e) {
		e.printStackTrace();
	}
}

	public static Map<String, List<ParentAccountVO>> processBankDepositData(
			String filePath) throws BankOrganizerException {
		BankDepositAccountOrganizer obj=new BankDepositAccountOrganizer();
		Map<String, List<ParentAccountVO>> outputmap=new HashMap();
		List<ParentAccountVO> accountListWM=new ArrayList();
		List<ParentAccountVO> accountListSAV=new ArrayList();
		List<ParentAccountVO> accountListNRI=new ArrayList();
		boolean isValid=false;
		FileReader filereader;
		try {
			filereader = new FileReader(filePath);
		} catch (FileNotFoundException e1) {
			throw new BankOrganizerException("File not found exception "+e1.getMessage());
		}
		BufferedReader reader=new BufferedReader(filereader);
		
		String line;
		try {
			line = reader.readLine();
			while(line!=null){
				String[] strarr=line.split("[,]");
				isValid=validateData(strarr);
				if(!isValid){
					throw new BankOrganizerException("Validation failed !!");
				}
				else{
					String parentAccNo=strarr[0];
					String name=strarr[1];
					String accType=strarr[2];
					
					ParentAccountVO parentAccount=new ParentAccountVO();
					parentAccount.setAccType(accType);
					parentAccount.setName(name);
					parentAccount.setParentAccNo(Integer.parseInt(parentAccNo));
					parentAccount.setLinkedDeposits(obj.formLinkedDepositList(filePath,parentAccNo));
					
					if("WM".equals(accType)){
						accountListWM.add(parentAccount);
					}
					else if("SAV".equals(accType)){
						accountListSAV.add(parentAccount);
					}
					else if("NRI".equals(accType)){
						accountListNRI.add(parentAccount);
					}
					
				}
				line=reader.readLine();
			}
			
			outputmap.put("WM", accountListWM);
			outputmap.put("SAV", accountListSAV);
			outputmap.put("NRI", accountListNRI);
			
		} catch (IOException e) {
			throw new BankOrganizerException("File io exception "+e.getMessage());
		}
		
		return outputmap;
	}

	private List<LinkedDepositVO> formLinkedDepositList(String filepath,String parentAccNo) throws BankOrganizerException, IOException{
		
		FileReader filereader;
		List<LinkedDepositVO> linkedaccList=new ArrayList<LinkedDepositVO>();
		try {
			filereader = new FileReader(filepath);
		} catch (FileNotFoundException e1) {
			throw new BankOrganizerException("File not found exception "+e1.getMessage());
		}
		BufferedReader reader=new BufferedReader(filereader);
		
		String strr=reader.readLine();
		while(strr!=null){
			String[] str=strr.split("[,]");
			String tempparentAccNo=str[0];
			String name=str[1];
			String accType=str[2];
			String linkedDepositAccNumber=str[3];
			String depositAmount=str[4];
			String depositStartDate=str[5];
			String depositMaturityDate=str[6];
			
			if(tempparentAccNo.equals(parentAccNo)){
				LinkedDepositVO linkedVO=new LinkedDepositVO();
				linkedVO.setDepositAmount(Integer.parseInt(depositAmount));
				linkedVO.setDepositMaturityDate(formatDate(depositMaturityDate));
				linkedVO.setDepositStartDate(formatDate(depositStartDate));
				linkedVO.setLinkedDepositNo(linkedDepositAccNumber);
				linkedVO.setMaturityAmount(calculateMaturityAmount(linkedVO.getDepositStartDate(),
						linkedVO.getDepositMaturityDate(),linkedVO.getDepositAmount()));
				linkedaccList.add(linkedVO);
			}
			strr=reader.readLine();
		}
		
		return linkedaccList;
	}
	 
	private Date formatDate(String tempDate){
		SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
		Date date=null;
		sdf.setLenient(false);
		try {
			date=sdf.parse(tempDate);
		} catch (ParseException e) {
			//throw new BankOrganizerException("Date format should be in dd-MM-yyyy format");
			System.out.println("Date format should be in dd-MM-yyyy format");
			
			
		}
		return date;
	}
	
	

	private float calculateMaturityAmount(Date date1, Date date2,int depositamount){
		float maturity_amount=0.00f;
		
		Calendar startDate=Calendar.getInstance();
		startDate.setTime(date1);
		
		Calendar endDate=Calendar.getInstance();
		endDate.setTime(date2);
		
		long diffInMillsec=endDate.getTimeInMillis()-startDate.getTimeInMillis();
		int noOfDays=(int)diffInMillsec/(1000*60*60*24);
		//Maturity Amount = Deposit Amount + (Deposit Amount * RateOfInterest/100)
		if(noOfDays>=0 && noOfDays<=200)//6.75
		{
			maturity_amount=(float) (depositamount + (depositamount*(6.75/100)));
		}
		else if(noOfDays>=201 && noOfDays<=400)//7.5
		{
			maturity_amount=(float) (depositamount + (depositamount*(7.5/100)));
		}
		else if(noOfDays>=401 && noOfDays<=600)//8.75
		{
			maturity_amount=(float) (depositamount + (depositamount*(8.75/100)));
		}
		else if(noOfDays>600)//10
		{
			maturity_amount=(float) (depositamount + (depositamount*(10/100)));
		}
		
		return maturity_amount;


	}

	public static boolean validateData(String[] str) {
		
		BankDepositAccountOrganizer obj=new BankDepositAccountOrganizer();
		boolean result=false;
		for(String s : str){
			if(s.isEmpty()){
				result=false;			
			}
		}
		
		String parentAccNo=str[0];
		String name=str[1];
		String accType=str[2];
		String linkedDepositAccNumber=str[3];
		String DepositAmount=str[4];
		String DepositStartDate=str[5];
		String DepositMaturityDate=str[6];
		System.out.println("ji");
		result=(
		obj.validateParentAccNumber(parentAccNo) &&
		obj.validateDate(DepositStartDate) &&
		obj.validateDate(DepositMaturityDate) &&
		obj.validateAccountType(accType) &&
		obj.validateLinkedDepositAccNumber(linkedDepositAccNumber));
		System.out.println(result);
		return result;
	}
	
	private boolean validateLinkedDepositAccNumber(String linkedDepositAccNumber){
		
		//System.out.println("deposit num");
		/*if(!Pattern.matches("[F][D][-][0-9]+",linkedDepositAccNumber) && !Pattern.matches("[R][D][-][0-9]+",linkedDepositAccNumber)
				&& !Pattern.matches("[M][U][T][-][0-9]+",linkedDepositAccNumber)){
			throw new BankOrganizerException("TheLinkedDepositAccNumber : "+linkedDepositAccNumber+" should start with FD/RD/MUT");
		}*/
		if(!linkedDepositAccNumber.startsWith("FD") && !linkedDepositAccNumber.startsWith("RD") 
				&& !linkedDepositAccNumber.startsWith("MUT")){
			//throw new BankOrganizerException("TheLinkedDepositAccNumber : "+linkedDepositAccNumber+" should start with FD/RD/MUT");
			System.out.println("TheLinkedDepositAccNumber : "+linkedDepositAccNumber+" should start with FD/RD/MUT");
			return false;
		}
		return true;
	}
	private boolean validateAccountType(String accType){
		//System.out.println("acc type");
		if(!Arrays.asList("WM","SAV","NRI").contains(accType)){
			System.out.println("inside");
			//throw new BankOrganizerException("Parent Account : "+accType+" invalid");	
			System.out.println("Parent Account : "+accType+" invalid");
			return false;
		}
		return true;
	}
	private boolean validateParentAccNumber(String parentAccNo){
		//The account number should be numeric and should not start with 0
//System.out.println("parenty acc number");
		if(!Pattern.matches("[^0][0-9]+",parentAccNo)){
			//throw new BankOrganizerException("The account number should be numeric and should not start with 0  "+parentAccNo);
			System.out.println("The account number should be numeric and should not start with 0  "+parentAccNo);
			return false;
		}
		return true;
	}
	
	private boolean validateDate(String strdate){
		//System.out.println("validate datwe");
		SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
		sdf.setLenient(false);
		try {
			Date date=sdf.parse(strdate);
		} catch (ParseException e) {
			//throw new BankOrganizerException("Date format should be in dd-MM-yyyy format");
			System.out.println("Date format should be in dd-MM-yyyy format");
			return false;
			
		}
		return true;
	}

}

class ParentAccountVO {

	private int parentAccNo;
	private String name;
	private String AccType;
	//private LinkedDepositVO linkedDeposit;
	private List<LinkedDepositVO> linkedDeposits;

	public int getParentAccNo() {
		return parentAccNo;
	}

	public void setParentAccNo(int parentAccNo) {
		this.parentAccNo = parentAccNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccType() {
		return AccType;
	}

	public void setAccType(String accType) {
		AccType = accType;
	}

	public List<LinkedDepositVO> getLinkedDeposits() {
		return linkedDeposits;
	}

	public void setLinkedDeposits(List<LinkedDepositVO> linkedDeposits) {
		this.linkedDeposits = linkedDeposits;
	}

	public boolean equals(Object object) {
		boolean isEqual = false;
		ParentAccountVO otherAccount = (ParentAccountVO) object;
		if ((this.parentAccNo == otherAccount.parentAccNo)
				&& (this.AccType.equals(otherAccount.getAccType()) && (this.linkedDeposits
						.equals(otherAccount.getLinkedDeposits())))) {
			isEqual = true;
		}
		return isEqual;
	}



	@Override
	public String toString() {
		return "ParentAccountVO [parentAccNo=" + parentAccNo + ", name=" + name
				+ ", AccType=" + AccType + ", linkedDeposits=" + linkedDeposits
				+ "]";

		//	return parentAccNo  + "  , " +  name  + " ," + AccType + " ," +  linkedDeposits;

	}

}

class LinkedDepositVO {

	private String linkedDepositNo;
	private int depositAmount;
	private Date depositStartDate;
	private Date depositMaturityDate;
	private float maturityAmount;

	public String getLinkedDepositNo() {
		return linkedDepositNo;
	}

	public void setLinkedDepositNo(String linkedDepositNo) {
		this.linkedDepositNo = linkedDepositNo;
	}

	public int getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(int depositAmount) {
		this.depositAmount = depositAmount;
	}

	public Date getDepositStartDate() {
		return depositStartDate;
	}

	public void setDepositStartDate(Date depositStartDate) {
		this.depositStartDate = depositStartDate;
	}

	public Date getDepositMaturityDate() {
		return depositMaturityDate;
	}

	public void setDepositMaturityDate(Date depositMaturityDate) {
		this.depositMaturityDate = depositMaturityDate;
	}

	public float getMaturityAmount() {
		return maturityAmount;
	}

	public void setMaturityAmount(float maturityAmount) {
		this.maturityAmount = maturityAmount;
	}

	public boolean equals(Object object) {
		boolean isEquals = false;
		LinkedDepositVO depositVO = (LinkedDepositVO) object;
		if (this.linkedDepositNo.equals(depositVO.getLinkedDepositNo())
				&& (this.depositAmount == depositVO.getDepositAmount())
				&& (this.depositStartDate.equals(depositVO
						.getDepositStartDate()))
						&& (this.maturityAmount == depositVO.getMaturityAmount())) {
			isEquals = true;
		}
		return isEquals;
	}

	@Override
	public String toString() {


		return "LinkedDepositVO [linkedDepositNo=" + linkedDepositNo
				+ ", depositAmount=" + depositAmount + ", depositStartDate="
				+ depositStartDate + ", depositMaturityDate="
				+ depositMaturityDate + ", maturityAmount=" + maturityAmount
				+ "]"; 

		//	return linkedDepositNo  + "  , " +  depositAmount  + " ," + depositStartDate + " ," +  depositMaturityDate + "," + maturityAmount;
	}

}

class BankOrganizerException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BankOrganizerException(String message) {
		super(message);
	}

	public BankOrganizerException(Throwable throwable) {
		super(throwable);
	}

	public BankOrganizerException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

/************************************************************/
/*
 * DO NOT CHANGE THE BELOW CLASS. THIS IS FOR VERIFYING THE CLASS NAME AND
 * METHOD SIGNATURE USING REFLECTION APIs
 */
/************************************************************/
class Validator {

	private static final Logger LOG = Logger.getLogger("Validator");

	public Validator(String filePath, String className, String methodWithExcptn) {
		validateStructure(filePath, className, methodWithExcptn);
	}

	protected final void validateStructure(String filePath, String className,
			String methodWithExcptn) {

		if (validateClassName(className)) {
			validateMethodSignature(methodWithExcptn, className);
		}

	}

	protected final boolean validateClassName(String className) {

		boolean iscorrect = false;
		try {
			Class.forName(className);
			iscorrect = true;
			LOG.info("Class Name is correct");

		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, "You have changed either the "
					+ "class name/package. Use the default package "
					+ "and class name as provided in the skeleton");

		} catch (Exception e) {
			LOG.log(Level.SEVERE, "There is an error in validating the "
					+ "Class Name. Please manually verify that the "
					+ "Class name is same as skeleton before uploading");
		}
		return iscorrect;

	}

	protected final void validateMethodSignature(String methodWithExcptn,
			String className) {
		Class cls;
		try {

			String[] actualmethods = methodWithExcptn.split(",");
			boolean errorFlag = false;
			String[] methodSignature;
			String methodName = null;
			String returnType = null;

			for (String singleMethod : actualmethods) {
				boolean foundMethod = false;
				methodSignature = singleMethod.split(":");

				methodName = methodSignature[0];
				returnType = methodSignature[1];
				cls = Class.forName(className);
				Method[] methods = cls.getMethods();
				for (Method findMethod : methods) {
					if (methodName.equals(findMethod.getName())) {
						foundMethod = true;
						if ((findMethod.getExceptionTypes().length != 1)) {
							LOG.log(Level.SEVERE, "You have added/removed "
									+ "Exception from '" + methodName
									+ "' method. "
									+ "Please stick to the skeleton provided");
						}
						if (!(findMethod.getReturnType().getName()
								.equals(returnType))) {
							errorFlag = true;
							LOG.log(Level.SEVERE, " You have changed the "
									+ "return type in '" + methodName
									+ "' method. Please stick to the "
									+ "skeleton provided");

						}

					}
				}
				if (!foundMethod) {
					errorFlag = true;
					LOG.log(Level.SEVERE,
							" Unable to find the given public method "
									+ methodName + ". Do not change the "
									+ "given public method name. "
									+ "Verify it with the skeleton");
				}

			}
			if (!errorFlag) {
				LOG.info("Method signature is valid");
			}

		} catch (Exception e){

		}
	}
}
