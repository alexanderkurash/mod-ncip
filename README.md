# mod-ncip

Copyright (C) 2019-2020 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

NISO Circulation Interchange Protocol (NCIP)  support in FOLIO


## Preparation
1. The NCIP module requires a FOLIO user with the following permissions:
```
    ncip.all
    inventory-storage.items.collection.get
    ui-circulation.settings.overdue-fines-policies
    ui-circulation.settings.lost-item-fees-policies
    
```
2. If you will be exposing this service externally and will be using the [edge-ncip module](https://github.com/folio-org/edge-ncip), you will need to setup an API key as described [in the readme file of the edge-common module](https://github.com/folio-org/edge-common)

3. There are settings that have to be setup in mod-configuration for the NCIP services to work (more about that below).  The values assigned to these settings must exist in FOLIO.  This is because FOLIO requires specific values to be set when actions occur.  For example, the AcceptItem service creates an instance.  The NCIP module has to know what instance.type.name to use. Here is a list of the configurations you will need to establish values for in FOLIO:

    * (1) instance.type.name
    * (2) instance.source
    * (3) item.material.type.name
    * (4) item.perm.loan.type.name
    * (5) item.status.name
    * (6) item.perm.location.code
    * (7) holdings.perm.location.code
    * (8) instance.custom.identifier.name
    * (9) checkout.service.point.code
    * (10)checkin.service.point.code

Notes 
* You can assign different values to these settings per Agency ID used in the NCIP requests.  This approach lets you setup different values for differnt Agency IDs.  For example, if Relais calls your NCIP server with the Agency ID of 'Relais' you can configure values for that agency.  If ReShare calls your NCIP server using a different Agency ID, you can set up different configuration values to be used for ReShare requests.  These settings have to exist for each Agency ID that will be used in the NCIP requests.


* The screen prints below illustrate how these values are used by the NCIP module on the instance, holdings and item records:

![Illustrates how the NCIP property values will be used on the instance record](docs/images/instanceNcipExample.png?raw=true "Illustrates how the NCIP property values will be used on the instance record")

![Illustrates how the NCIP property values will be used on the item record](docs/images/ncipItemExample.png?raw=true "Illustrates how the NCIP property values will be used on the item record")

 
## Installing the module


This module does have a companion 'edge' module - edge-ncip - that can be used to expose this service to external applications.
https://github.com/folio-org/edge-ncip

### Configuration


<table>
  <tr>
   <td>config option
   </td>
   <td>type
   </td>
   <td>description
   </td>
  </tr>
  <tr>
   <td>port
   </td>
   <td>int
   </td>
   <td>The port the module binds to.  The default is 8081
   </td>
  </tr>
  <tr>
   <td>okapi_url
   </td>
   <td>string
   </td>
   <td>You may not need this if you are accessing the NCIP module through the edge-ncip module
   </td>
  </tr>
</table>




## mod-configuration setup

There are three types of settings that can exist in mod-configuration for the NCIP module:

### Required Configurations:
1) NCIP properties: these are the settings required for the NCIP services to work.  See explanation above in the 'preparation' section of this README file.
### Optional Configurations
2) XC NCIP Toolkit properties:  While there are examples of these properties below YOU DO NOT HAVE TO SET THEM.  The NCIP module will use these as default values.  You can override them in mod-configuration if you need to.
3) Rule properties: Use these setting if you want the LookupUser service to use two rules when determining if a patron can borrow.  They are max fine amount and max loan count.  YOU DON'T HAVE TO SET THESE RULES if you don't want to use them.  The lookup user service will function even if they are not set. The LookupUser service will look for blocks on the patron and the active/inactive indicator.  If you also want it to consider limits on fines and checked out items you can configuration these rules.  

#### NCIP Properties Examples

| MODULE        | configName (the AgencyID)   |   code          | value  (examples) |   
| ------------- |:-------------:| :-----------------------------|------------------:|		
| NCIP          | Relais 		| instance.type.name 			| RESHARE             |	
| NCIP          | Relais     	| instance.source 				| RESHARE             |	
| NCIP          | Relais      	| item.material.type.name 		| RESHARE             |	
| NCIP          | Relais 		| item.perm.loan.type.name 		| RESHARE             |	
| NCIP          | Relais     	| item.status.name   			| Available         |
| NCIP          | Relais      	| item.perm.location.code 		| RESHARE_DATALOGISK      |	
| NCIP          | Relais 		| holdings.perm.location.code 	| RESHARE_DATALOGISK      |	
| NCIP          | Relais     	| instance.custom.identifier.name| ReShare Request ID |		
| NCIP          | Relais      	| checkout.service.point.code	| cd2               |		
| NCIP          | Relais      	| checkin.service.point.code 	| cd2               |		

You will need a set of these settings in mod-configuration for each individual Agency ID making NCIP requests.  Example of an AgencyID in an NCIP request:
   

![Illustrates NCIP message pointing out the agency ID](docs/images/ncipMessageIllustratesAgencyId.png?raw=true "Illustrates NCIP message pointing out the agency ID")


When the module is started the default Toolkit property files are initialized.  When the first request is received by mod-ncip (per tenant) the configuration values from mod-configuration are initialized.  This means the first request may be a bit slow to respond.

If you later add settings to mod-configuration you can initialize them in mod-ncip by calling these endpoints:

* To reinitialize the NCIP properties --> send a GET request to ../ncip/initncipproperties
* To reinitialize the Toolkit properties --> send a GET request to ../ncip/inittoolkit
* To reinitialize the Rules properties --> send a GET request to ../ncip/initrules


As you are setting up mod-nicp, the NCIP properties and the settings values in FOLIO, you can use this utility service to validate the NCIP property values you have set (it attempts to look up each value you have configured):

* To validate your configuration settings --> send a GET request to ../ncipconfigcheck

If the service is able to retrieve a UUID for each of the settings it will send back an “ok” string.  If it cannot locate any of the settings it will return an error message to let you know which setting it couldn’t find.

    
    <Problem>
        <message>problem processing NCIP request</message>
        <exception>java.lang.Exception: The lookup of PALCI_NOTREAL could not be found for relais.instance.type.name</exception>
    </Problem>

    
## About the NCIP services
This initial version of the NCIP module supports four of the existing 50ish services in the NCIP protocol.  The endpoint for all of the services is the same:

POST to http://yourokapiendoint/ncip    (if you are calling the mod-ncip directly)

POST to http://youredgencipendpoint/ncip (if you are calling mod-ncip through edge-ncip)

The module determines which service is being called based on the XML passed into the service.
These particular four services were selected because they are required to interact with the D2D software that supports the ILL service that several participating libraries currently use.  Mod-NCIP was written using the Extensible Catalog (XC) NCIP toolkit (more about this below).  This means that adding additional services to this module should mainly involve writing the code that calls the FOLIO web services.  The 'plumbing' that translates the XML to objects and back to XML is built into the toolkit for all of the NCIP messages in the protocol.

#### Supported Services

##### Lookup User
The lookup user service determines whether or not a patron is permitted to borrow.  The response can include details about the patron and will also include a "blocked" or "active" value to indicate whether or not a patron can borrow.  The service looks for 'blocks' assigned to the patron.  It also looks at the patron 'active' indicator.

This service can also use the Drools rules to help determine the 'blocked' or 'active' value for the response.  The Drools rules look at the number of items checked out and the amount of outstanding fines.  The values used in the rules can be adjusted in mod-configuration.  If you don't want to use them skip setting any values for them in mod-configuration.

Sample XML Request:

https://github.com/folio-org/mod-ncip/blob/master/docs/sampleNcipMessages/lookupUser.xml

##### Accept Item
The accept item service is called when a requested item arrives from another library.  This service essentially creates the temporary record and places it on hold.
It is probably the most complicated of the existing four service.  It:

1. Creates an instance (which is set as 'Suppress from discovery')
2. Creates a holding record (which is set as 'Suppress from discovery')
3. Creates an item (which is set as 'Suppress from discovery')
4. Places a hold (page request) on the item for the patron

If something goes wrong with any of these four steps it does attempt to delete any instance, holding or item that may have been created along the way. 

In regards to placing the hold, the 'Requests' module has a 'Pickup Preference' field with options - 'hold shelf' or 'delivery'.  The NCIP request contains 'PickupLocation'.  I thought about a configuration that would allow the service to translate a pickup location to Pickup Preference = delivery.  
However, when Pickup Preference = delivery, a patron delivery address is required.  I don't think there is a way for me to derive that location - so I've hardcoded pickup preference to 'hold shelf'.
The "PickupLocation" that is included in the request is recorded in the FOLIO "Pickup Service Point" field.  For example...in our current NCIP implementation using OLE at Lehigh, we support three pickup locations (Linderman, Fairchild and Delivery).  I'm guessing that will stay the same for our FOLIO implemenation.  One of those three will be recorded in the FOLIO "Pickup Service Point" field.  


Sample XML Request:

https://github.com/folio-org/mod-ncip/blob/master/docs/sampleNcipMessages/acceptItem.xml

##### Checkout Item
The checkout item service is called when an item is checked out (either a temporary item being circulated to a local patron or a local item being loaned to another library).
In the 1.0 version of this module, this service does check for blocks on the patron and looks at the active indicator.  If if finds blocks or if the patron is not 'active' the call to the service will fail.  If/when JIRA UXPROD-1683 is completed this check can be removed.

Sample XML Request:

https://github.com/folio-org/mod-ncip/blob/master/docs/sampleNcipMessages/checkOutItem.xml

##### Checkin Item
The checkin item service is called when an item is checked in.  This service can include patron information in the response.  However, if the CheckInItem service is called and there is not an outstanding loan, no patron information will be included in the response. 

Sample XML Request:

https://github.com/folio-org/mod-ncip/blob/master/docs/sampleNcipMessages/checkInItem.xml

### About the Extensible Catalog NCIP Toolkit

The eXtensible Catalog (XC) NCIP Toolkit was developed as a stand-alone Web application that would receive NCIP requests, communicate with your ILS (via a ‘connector’) and send back an XML response.

The FOLIO “mod-ncip” module merges the OKAPI microservices framework with the classes from the core XC NCIP Toolkit project.  The core XC NCIP toolkit Java libraries provide functionality to transform incoming XML (from the NCIP request) to objects, shepards each request to a specific service and then transforms objects back to XML for the response. 

The XC NCIP Toolkit supports all of the services in the [NCIP 2 protocol](http://www.ncip.info/uploads/7/1/4/6/7146749/z39-83-1-2012_ncip.pdf).  This means adding new services to the FOLIO module will involve a small amount of setup plus writing the code that will call the FOLIO APIs to process the request.

### Adding support for additional services to mod-ncip
To illustrate the steps required to add support for additional services I've used the "Request Item" service as an example:

#### Step 1: Update the toolkit.properties file
Update the toolkit.properties file with the new service name pointing it to the class that you will create which will process the requests for this service:

Note: Correct capitalization is important for this configuration.  CheckinItemService did not work.  CheckInItemService worked and it took a bit to figure out what the problem was.

![Illustrates updating the toolkit.properties file by adding a configuration for the Request Item Service](docs/images/newServiceToolkit.png?raw=true "Illustrates updating the toolkit.properties file")

These are the default values used for the NCIP Toolkit configuraiton.

#### Step 2: Create the class you configured in step 1
The new class should implement the Toolkit's interface for this service.  In this example your new class would implement the RequestItemService interface.  This means your class is required to have a 'performService' method as illustrated below.
When an NCIP request is received, the toolkit looks at the XML in the body of the request to to decide which class will process it (based on the toolkit configuraiton).  For example, if a request comes in that contains the RequestItem Service XML, the toolkit will instantiate your implemenation of the RequestItem service and then the performService method will be called.  You can see this in the 'ncipProcess' method in the FolioNcipHelper class.

![Illustrates the new FolioRequestItemService class](docs/images/requestItemService.png?raw=true "Illustrates the new FolioRequestItemService class")

The RequestItemInitiationData (in this example) contains all of the values that were contained in the XML in the body of the request. The RequestItemInitiationData object should contains values like 'requestType' and 'itemIds'.

The performService method is responsible for returning the response data object (in this example RequestItemResponseData).  This is the object that will be transformed into the XML that will be included in the response. 

The RemoteServiceManager interface has no methods.  The methods written for this class should be whatever is needed for your implementation.  More about this class from the XC documentation:
 
 "The methods that are written for the RemoteServiceManager implementation class are whatever is needed by the implementations of NCIPService (e.g. LookupItemService, RequestItemService, etc.). It’s certainly possible to put all of the functionality required to access the ILS in the implementations of NCIPService, and that might make sense. But what the RemoteServiceManager provides is a shared object for accessing the ILS, in case you need that to maintain state, cache objects..."
 
 In FOLIO's mod-ncip module, the FolioRemoteServiceManager class is the point where the FOLIO APIs (like check out item) are called.  You can continue this pattern for your new service if you think it makes sense.  The example above illustrates the request item service calling the RemoteServiceManagers 'requestItem' method.  The FolioRemoteServiceManager class contains all of the NCIP property values.
 
 

#### Step 4: Create a method in the FolioRemoteServiceManager class 
Create a method (or methods) in the FolioRemoteServiceManager class that will take care of the interaction with the FOLIO API.  The FolioRequestItemService (performService method) could then call this method.

You can look at the existing services for examples.  The FolioCheckInItemService calls the FolioRemoteServiceManager 'checkIn' method after some initial validation.  This method passes back the required data for the response.  The FolioCheckInItemService contructs the ResponseData object and returns it.  The toolkit takes care of transforming that into XML.  (see createResponseMessageStream in the FolioNcipHelper class).

![Illustrates the new FolioRemoteServiceManager class](docs/images/remoteServiceManager.png?raw=true "Illustrates FolioRemoteServiceManager class")

#### XC NCIP Toolkit - Additional resources
[http://catalogablog.blogspot.com/2009/03/extensible-catalog-ncip-toolkit.html](http://catalogablog.blogspot.com/2009/03/extensible-catalog-ncip-toolkit.html)

[https://web.archive.org/web/20160416142842/http://code.google.com/p/xcncip2toolkit/wiki/DriverSummary](https://web.archive.org/web/20160416142842/http://code.google.com/p/xcncip2toolkit/wiki/DriverSummary)

[https://web.archive.org/web/20130508170543/http://www.extensiblecatalog.org/news/oclc-contributes-ncip-20-code-xc-ncip-toolkit](https://web.archive.org/web/20130508170543/http://www.extensiblecatalog.org/news/oclc-contributes-ncip-20-code-xc-ncip-toolkit)

[https://code.google.com/archive/p/xcnciptoolkit/](https://code.google.com/archive/p/xcnciptoolkit/)

[https://www.carli.illinois.edu/frequently-asked-questions-about-xc](https://www.carli.illinois.edu/frequently-asked-questions-about-xc)

[https://www.oclc.org/developer/news/2010/developer-collaboration-leads-to-implementation-of-ncip-20.en.html](https://www.oclc.org/developer/news/2010/developer-collaboration-leads-to-implementation-of-ncip-20.en.html) 

### More about the toolkit settings
https://github.com/eXtensibleCatalog/NCIP2-Toolkit/wiki/GeneralConfiguration
https://github.com/moravianlibrary/xcncip2toolkit/blob/master/connectors/aleph/22/trunk/web/src/main/resources/toolkit.properties

## Additional information

### Issue tracker

See project [MODNCIP](https://issues.folio.org/browse/MODNCIP)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### ModuleDescriptor

See the built `target/ModuleDescriptor.json` for the interfaces that this module
requires and provides, the permissions, and the additional module metadata.

### Code analysis

[SonarQube analysis](https://sonarcloud.io/dashboard?id=org.folio%3Amod-ncip).

### Download and configuration

The built artifacts for this module are available.
See [configuration](https://dev.folio.org/download/artifacts) for repository access,
and the [Docker image](https://hub.docker.com/r/folioorg/mod-ncip/).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)

