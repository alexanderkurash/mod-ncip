/**
 * 
 */
package org.folio.ncip;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.hamcrest.Matchers;



import org.junit.Before;
import org.junit.Test;

import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.response.Response;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.path.xml.XmlPath.CompatibilityMode;




/**
 * @author 
 *
 */
public class LookupUser extends TestBase {
	


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void callLookupUserSuccess() throws MalformedURLException {
			Response response = postData("src/test/resources/mockdata/ncip-lookupUser.xml");
			response.then().assertThat().body("NCIPMessage.LookupUserResponse.UserId.UserIdentifierValue", Matchers.equalTo(TestConstants.GOOD_PATRON_BARCODE));
			response.then().assertThat().body("NCIPMessage.LookupUserResponse.UserOptionalFields.UserPrivilege[2].UserPrivilegeStatus.UserPrivilegeStatusType", Matchers.equalTo(TestConstants.OK));
			System.out.println(response.getBody().prettyPrint());
	}
	
	@Test
	public void callLookupUserNotFound() throws MalformedURLException {
		Response response = postData("src/test/resources/mockdata/ncip-lookupUserNotFound.xml");
		response.then().assertThat().body("NCIPMessage.LookupUserResponse.Problem", Matchers.not(Matchers.isEmptyOrNullString()));
		response.then().assertThat().body("NCIPMessage.LookupUserResponse.Problem.ProblemValue", Matchers.containsString(TestConstants.DID_NOT_FIND));
		System.out.println(response.getBody().prettyPrint());
	}
	
	@Test
	public void callLookupUserBlocked() throws MalformedURLException {
		Response response = postData("src/test/resources/mockdata/ncip-lookupUserBlocked.xml");
		response.then().assertThat().body("NCIPMessage.LookupUserResponse.UserId.UserIdentifierValue", Matchers.equalTo(TestConstants.BLOCKED_PATRON_BARCODE));
		response.then().assertThat().body("NCIPMessage.LookupUserResponse.UserOptionalFields.UserPrivilege[2].UserPrivilegeStatus.UserPrivilegeStatusType", Matchers.equalTo(TestConstants.BLOCKED));
		System.out.println(response.getBody().prettyPrint());
	}
	
	@Test
	public void callLookupUserBlockedByRule() throws MalformedURLException {
		Response response = postData("src/test/resources/mockdata/ncip-lookupUserBlockedFine.xml");
		response.then().assertThat().body("NCIPMessage.LookupUserResponse.UserId.UserIdentifierValue", Matchers.equalTo(TestConstants.BLOCKED_PATRON_BARCODE_BY_RULES));
		response.then().assertThat().body("NCIPMessage.LookupUserResponse.UserOptionalFields.UserPrivilege[2].UserPrivilegeStatus.UserPrivilegeStatusType", Matchers.equalTo(TestConstants.BLOCKED));
		System.out.println(response.getBody().prettyPrint());
	}



}
