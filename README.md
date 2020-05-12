# YouTransactor mPOS SDK - Android
###### Release 3.0.0.0

![Cptr_PlatformAPI](https://user-images.githubusercontent.com/59020462/71244593-2b897180-2313-11ea-95af-8a2fcce628eb.jpeg)

This repository provides a step by step documentation for YouTransactor's native Android SDK, that enables you to integrate our proprietary card terminal(s) to accept credit and debit card payments (incl. VISA, MasterCard, American Express and more). The relation between the mobile device and the card terminal is a Master-Slave relation, so the mobile device drives the card terminal by calling diffrent available RPC. The main function of the SDK is to send RPC commands to the card terminal in order to drive it. The SDK provides also a payment, update and log APIs. 

The SDK contains several modules: Connexion, RPC, MDM, Payment, Log.
* The connexion module provide an interface 'IconnexionManager' so you can use your implementation and also it provide a Bluetooth implementaions (classical bluetooth ans BLE).
* The RPC module use the IconnexionManager implementation to send/receive, RPC command/response from card terminal. It provide an implementation of all RPC Commands you will see next how to use that in your application.
* The MDM module is an implementation of all YouTransaction's TMS services. The TMS server is mainly used to manage the version of firmware and ICC / NFC configurations of card terminal. So the SDK allows you to transparently update of the card terminal using our TMS. This module is useless if you decide to use another TMS not the YouTransactor one.
* The payment module implements the transaction processing for contact and contactless. For every payment, a UCubePaymentRequest instance should be provided as input to configure the current payment and durring the transaction a callback is returned for every step. At the end of transaction a PaymentContext instance is returned which contains all necessary data to save the transaction. An example of Payment call is provided next.
* The SDK provide an ILogger interface and a default implementation to manage logs. Your application has the choice between using the default implementation which print the logs in a file that can be sent to our TMS server or you can use you own implemantation of ILogger. 

All this functions are resumed in one Class which is UCubeAPI. This class provides public static methods that your application can use to setup ConnexionManager, setup Logger, do a payment, do an update using Our TMS...

The SDK do not perciste any connexion or transaction or update data. 

For more information about YouTransactor developer products, please refer to our [www.youtransactor.com](https://www.youtransactor.com).

## I. General overview 

### 1. Introduction

YouTransactor mPOS card terminals are : 
* uCube ( with differents models )
* uCube Touch

The uCube Touch is a new version of the uCube. There are some hardware differences, like: 
* The uCube use the classical Bluetooth and the uCube Touch use the BLE 
* The uCube provide a magstripe reader but not the uCube Touch
* ...

For the SDK, there is no difference betwen all YouTransactor's card terminals. For example, if you integrate the uCube Touch, at the beginning you should use UCubeAPI to setup a BLE Connexion, and if you intergrate the uCube, you should setup a Bt classic connexion manager. So the RPC module will use the connexion manager instance that you choose to send/receive data from terminal. 

### 2. uCube

The uCube is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a Bluetooth connection to enable acceptance of magstripe, contactless and smart payment cards (depending on the model).

<p align="center">
  <img width="200" height="250" src="https://user-images.githubusercontent.com/59020462/76528252-cd32e180-6470-11ea-9182-742faca82167.png">
</p>

### 3. uCube Touch

The uCube Touch is a lightweight and compact payment dongle. It can turn a tablet or a mobile device, Android or iOS, into a point of sale, via a BLE connection to enable acceptance of contactless and smart payment cards.

<p align="center">
  <img width="250" height="250" src="https://user-images.githubusercontent.com/59020462/77367842-437df080-6d5b-11ea-8e3a-423c3bc6b96b.png">
</p>

### 4. Mobile Device

The mobile device can be either Android or iOS and typically hosts applications related to payment. It links the uCube / uCube Touch to the rest of the system.

The mobile device application consists of 2 modules:
* Business module
	* Application that meets the business needs of the end customer. This is for example a cashier    	    application in the case of a restaurant, or a control application in the case of transports.
* Payment Module
	* Drives the transaction
	* Responsible for device software/configurations updates

The business module on the mobile device is developed by the integrator. It uses the user interfaces of the mobile device to fulfill the business needs of the customer.

The Payment module integrates our SDK, which is delivered as a library, and compiled with the payment module to generate the payment application.

The purpose of this document is to describe the services provided by the SDK to the payment module.

### 5. The Management System

The management system can be administered by YouTransactor and offers the following services:
* Management of the uCube fleet
* Deployment of software updates
* Deployment of payment parameters
* Other services

The MDM module of SDK implements all our management system services and the UCubeAPI provides API to call this implementation. Examples are provided next in this documentation.

### 6. Terminal management

#### 6.1 Initial configuration  

To be functional, in the scope of PCI PTS requirement, and SRED key shall be loaded securely in the device. This key is loaded locally by YouTransactor tools. The initial SALT is injected in the same way.

#### 6.2 Switching On/Off

The uCube lights up by pressing the "ON / OFF" button for three seconds. Once the device is on, the payment module can detect it, and initiate the payment process. The uCube switches off either by pressing the "ON / OFF" button or after X* minutes of inactivity (* X = OFF timeout).

The uCube Touch can be lights up exactly like the uCube, but also by using ` connect`  method of the connexion manager. When connection established, the SDK checks the terminal's state, if it 's power off, it turns it ON. 

#### 6.3 Update

During the life of the terminal, the firmware could be updated (to get bug fix, evolution..), the contact and contactless configuration also could be updated. The Terminal's documentation describe how those updates can be done and which RPC to use to do that.
If you will use our TMS, this can be done transparentlly by calling first the ` mdmCheckUpdate`  API to get the TMS configuration and compare it with current versions, then the ` mdmUpdate`  to do the update.

#### 6.4 System logs

The SDK print logs in logcat at runtime. The log module use a default ILoggger implementation that print these logs in a file which can be sent afterwards to a remote server. Our TMS provides a WS to receive a zip of log files.
So you can setup the log module to use the default implementation or your own implementation. 

## II. Technical Overview

### 1. General Architecture

This diagrams describes the general YouTransactor MPOS Android SDK architecture. Only the uCubeAPI methods and the RPC commands are public and you can call them. 

![sdk_architecture](https://user-images.githubusercontent.com/59020462/81673044-5489da80-944b-11ea-95a1-ffff128a43e9.png)

### 2. Transaction Flow : Contact

<p align="center">
  <img width="300" height="300" src="https://user-images.githubusercontent.com/59020462/71239375-b44de080-2306-11ea-9c32-f275a5407801.jpeg">
</p>

### 3. Transaction Flow : Contactless

<p align="center">
  <img width="300" height="300" src="https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg">
</p>

![Cptr_TransactionNFC](https://user-images.githubusercontent.com/59020462/71239723-8ddc7500-2307-11ea-9f07-2f4b11b42620.jpeg)

### 4. Prerequisites

To embed the package that you need in your application, you have to be sure of certain things in your settings.
1. Received YouTransactor card terminal : uCube or uCubeTouch
2. The `minSDKVersion` must be at 21 or later to works properly.
3. The `targetSDKversion` 28 or later (as a consequence of the migration to AndroidX).
4. The `Android plugin for Gradle` must be at 3.3.0 or later.
For more information about AndroidX and how to migrate see Google AndroidX Documentation.

### 5. Dependency

The SDK is in the format “.aar” library. You have to copy paste it in your app/libs package. So if you want to access to it you will need to get into your app-level Build.Gradle to add this dependency:

		implementation files('libs/libApp.aar')

### 6. UCubeAPI

The APIs provided by UCubeAPI are:

```java

	setConnexionManager(@NonNull IConnexionManager connexionManager)
	setupLogger(@NonNull Context context, @Nullable ILogger logger)
	pay(@NonNull Activity activity, @NonNull UCubePaymentRequest uCubePaymentRequest, @NonNull UCubeLibPaymentServiceListener listener)
	close()
	
	/* YouTransactor TMS APIs*/
	mdmSetup(@NonNull Context context)
	mdmRegister(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmUnregister(@NonNull Context context)
	isMdmManagerReady()
	mdmCheckUpdate(@NonNull Activity activity, boolean forceUpdate, boolean checkOnlyFirmwareVersion, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmUpdate(@NonNull Activity activity, final @NonNull List<BinaryUpdate> updateList, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)
	mdmSendLogs(@NonNull Activity activity, @Nonnull UCubeLibMDMServiceListener uCubeLibMDMServiceListener)

```

* You can use the sample app provided in this repository as a reference

#### initManagers (...)

* This API initializes the SDK by initializing differents modules; RPC, Payment, MDM…. It should be called in the begining, before calling any other API. 

```java
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		   super.onCreate(savedInstanceState);
		   setContentView(R.layout.activity_main);
		   UCubeAPI.initManagers(getApplicationContext());
		...
```

#### setup (...)

* It takes in input the YTMPOSProduct that user of SDK chooses to use.
* It can throw two types of exception: BleNotSupportException and BluetoothNotSupportException.
* BleNotSupportException : mean that the YTMPOSProduct specified was the uCube_Touch and the used smartphone don’t support BLE.
* BluetoothNotSupportException : mean that the used smartphone doesn’t support Bluetooth.  
* It can throws an Exception if the `initManagers` API not already called.

```java
		try {
		   UCubeAPI.setup(getApplicationContext(), this, YTMPOSProduct.uCube, new UCubeAPIListener() {
		       @Override
		       public void onProgress(UCubeAPIState uCubeAPIState) {
		       //TODO 
		       }
		       @Override
		       public void onFinish(boolean status) {
		       //TODO
		       }
		   });
		} catch (Exception e) {
		  e.printStackTrace();
		}
		...
```

#### getYTMPOSProduct ()

* This API returns the configured YTMPOSProduct if setup already called otherwise it returns null
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   ytmposProduct  = UCubeAPI.getYTMPOSProduct();
		   if (ytmposProduct != null) {
		       switch (ytmposProduct) {
			   case uCube:
				//TODO
			       break;
			   case uCube_touch:
				//TODO
			       break;
		       }
		   }
		} catch (Exception e) {
		   e.printStackTrace();
		}
		...
```

#### scanUCubeDevices (...)

* This API start a Bluetooth scan (classical Bluetooth or BLE depends of which product was selected) and return a call back: 
	* When uCube scanned
	* When scan finished
	* When error occured
* It can throw an Exception if the “initManagers” method not already called.

```java
	try {
		UCubeAPI.scanUCubeDevices(this, new UCubeAPIScanListener() {
                    @Override
                    public void onError() {
                    //TODO
                    }

                    @Override
                    public void onDeviceDiscovered(UCubeDevice uCubeDevice) {
  		    //TODO              
   		    }

                    @Override
                    public void onScanComplete(List<UCubeDevice> discoveredUCubeDevices) {
                    //TODO
                    }
                });
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...
```

#### stopScan

* Should be used to force stopping scan, for example before stopping activity. 
* It can throw an Exception if the “initManagers” method not already called.

```java
	try {
		UCubeAPI.stopScan();
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...
```

#### connect (...)

* This API connect the paired uCube if there is already one otherwise it does a Bluetooth scan and the user should select one device. it connects it and save it. It registers the device in the MDM and get the MDM-CLIENT certificate of the device. To be used for the double-authentication when calling others MDM WS.
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   UCubeAPI.connect(this, uCubeDevice, new UCubeConnectListener() {
		       @Override
		       public void onProgress(UCubeAPIState uCubeAPIState) {
			//TODO
		       }
		       @Override
		       public void onFinish(boolean status, UCubeInfo uCubeInfo) {
			//TODO
		       }
		   });
		} catch (Exception e) {
		   e.printStackTrace();
		}
		...

```
#### isConnected ()

* This API return a true or false that indicate if a connection is establishing with selected uCube or not.
* It can throw an Exception if the “initManagers” method not already called.

```java
	boolean isConnected;
	try {
		isConnected = UCubeAPI.isConnected()
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...

```

#### disconnect (...)

* This API used to disconnect uCube. 
* It can throw an Exception if the “initManagers” method not already called.

```java
	try {
	   UCubeAPI.disconnect(this, new UCubeAPIListener() {
	       @Override
	       public void onProgress(UCubeAPIState uCubeAPIState) {
		//TODO
	       }
	       @Override
	       public void onFinish(boolean status) {
		//TODO
	       }
	   });
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...
```
#### deleteSelectedUCube(...)

* This API remove all saved information of selected uCube and notify managers.
* If a connection was established it disconnect the device first
* It can throw an Exception if the “initManagers” method not already called.

```java
	try {
	   UCubeAPI.deletSelectedUCube(this, new UCubeAPIListener() {
	       @Override
	       public void onProgress(UCubeAPIState uCubeAPIState) {
		//TODO
	       }
	       @Override
	       public void onFinish(boolean status) {
		//TODO
	       }
	   });
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...
```

#### getSelectedUCubeDevice()

* This API returns an UCubeDevice which contains bluetooth information of selected device. 
* It can throw an Exception if the “initManagers” method not already called.

```java
	UCubeDevice uCubeDevice = null;
	try {
	   uCubeDevice = UCubeAPI.getSelectedUCubeDevice();
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...
```

#### getUCubeInfo ()

* This API returns an UCubeInfo which contains all paired uCube informations if there is already a paired one otherwise it returns null.
* It can throw an Exception if the “initManagers” method not already called.

```java
		UCubeInfo deviceInfos = null;
		try {
		   deviceInfos = UCubeAPI.getUCubeInfo();
		   //TODO
		} catch (Exception e) {
		   e.printStackTrace();
		}
		….
```

#### sendBankParamToDevice (...)

* This API used to inject list of bank parameters to uCube. The injection method is to use the SRED key to protect bank parameters. This API takes a Prepare bank parameters task where using the KSN user should prepare his parameters. 
* It can throw an Exception if the “initManagers” method not already called.

```java
	try {
	   UCubeAPI.sendBankParamToDevice(this, prepareBankParametersTask, new UCubeAPIListener() {
	       @Override
	       public void onProgress(UCubeAPIState uCubeAPIState) {
		//TODO
	       }
	       @Override
	       public void onFinish(boolean status) {
		//TODO
	       }
	   });
	} catch (Exception e) {
	   e.printStackTrace();
	}
	...
```

#### pay (...)

* This API activate all available reader in device and call Payment service and it depends from which reader is used to read card the specific service is called.
* This API takes in input a UCubePaymentRequest and gives in output a UCubePaymentResponse. 
* It makes a payment using the defined context in UCubePaymentRequest.
* During the payment this context will change and progress. At the end of the payment this context will be part of the UCubePaymentResponse + others attributes like payment state.
* It can throw an Exception if the “initManagers” method not already called.

##### UCubePaymentRequest

```java
		UCubePaymentRequest paymentRequest = new UCubePaymentRequest.Builder()
		       .setAmount(amount)  // if amount not specified uCube will propose to enter the amount before start tx
		       .setCurrency(currency)  // CURRENCY_EUR or CURRENCY_USD or new Currency(iso_code, exponent, label) 
		       .setForceOnlinePin(true)
		       .setAuthorizationTask(new AuthorizationTask(this))  // Instance of class that implements  IAuthorizationTask.
		       .setRiskManagementTask(new RiskManagementTask(this))  // Instance of class that implements  IRiskManagementTask.
		       .setCardWaitTimeout(timeout) // in second exemple 30 means 30 seconds of timeout to wait card
		       .setTransactionType(trxType) // PURCHASE /  WITHDRAWAL  / REFUND /  PURCHASE_CASHBACK / MANUAL_CASH / INQUIRY
		       .setSystemFailureInfo(true)
		       .setSystemFailureInfo2(false)
		       .setPreferredLanguageList(Collections.singletonList("en"))
		       .setRequestedAuthorizationTagList(Constants.TAG_TVR, Constants.TAG_TSI)
		       .setRequestedSecuredTagList(Constants.TAG_TRACK2_EQU_DATA)
		       .setRequestedPlainTagList(Constants.TAG_MSR_BIN)
		       .build();
```

##### AuthorizationTask

```java
		public class AuthorizationTask implements IAuthorizationTask {
		  private byte[] authResponse;
		  private PaymentContext paymentContext;
		  @Override
		  public byte[] getAuthorizationResponse() {  return authResponse; }
		  @Override
		  public PaymentContext getContext() { return paymentContext;}
		  @Override
		  public void setContext(PaymentContext context) { this.paymentContext = context; }
		  @Override
		  public void execute(ITaskMonitor monitor) {
		   ...
		  }
		...
```

##### RiskManagementTask

```java
		public class RiskManagementTask implements IRiskManagementTask {
		  private PaymentContext paymentContext;
		  private byte[] tvr;
		  @Override
		  public byte[] getTVR() { return tvr; }
		  @Override
		  public PaymentContext getContext() {  return paymentContext; }
		  @Override
		  public void setContext(PaymentContext context) { this.paymentContext = context; }
		  @Override
		  public void execute(ITaskMonitor monitor) {
		   ...
		  }
		...
		}
```

##### Call API

```java
		try {
		 UCubeAPI.pay(this, paymentRequest, new UCubePaymentListener() {
		   @Override
		   public void onStart(byte[] ksn) {
		       Log.d(TAG, "KSN : " + Arrays.toString(ksn));
		       //TODO Send KSN to the acquirer server
		   }
		   @Override
		   public void onFinish(boolean status, UCubePaymentResponse uCubePaymentResponse) {
		if (status && uCubePaymentResponse != null) {
			   Log.d(TAG, "Payment status : " + uCubePaymentResponse.paymentState);
			   Log.d(TAG, "ucube name: " + uCubePaymentResponse.uCube.ucubeName);
			   Log.d(TAG, "ucube address: " + uCubePaymentResponse.uCube.ucubeAddress);
			   Log.d(TAG, "ucube part number: " + uCubePaymentResponse.uCube.ucubePartNumber);
			   Log.d(TAG, "card label: " + uCubePaymentResponse.cardLabel);
			   Log.d(TAG, "amount: " + uCubePaymentResponse.paymentContext.getAmount());
			   Log.d(TAG, "currency: " + uCubePaymentResponse.paymentContext.getCurrency().getLabel());
			   Log.d(TAG, "tx date: " + uCubePaymentResponse.paymentContext.getTransactionDate());
			   Log.d(TAG, "tx type: " + uCubePaymentResponse.paymentContext.getTransactionType().getLabel());
			   if (uCubePaymentResponse.paymentContext.getSelectedApplication() != null) {
			       Log.d(TAG, "app ID: " + uCubePaymentResponse.paymentContext.getSelectedApplication().getLabel());
			       Log.d(TAG, "app version: " + uCubePaymentResponse.paymentContext.getApplicationVersion());
			   }
			   Log.d(TAG, "system failure log1: " + bytesToHex(uCubePaymentResponse.paymentContext.getSystemFailureInfo()));
			   Log.d(TAG, "system failure log2: " + bytesToHex(uCubePaymentResponse.paymentContext.getSystemFailureInfo2()));
			   if (uCubePaymentResponse.paymentContext.getPlainTagTLV() != null)
			       for (Integer tag : uCubePaymentResponse.paymentContext.getPlainTagTLV().keySet())
					 Log.d(TAG, "Plain Tag : " + tag + " : " + bytesToHex(uCubePaymentResponse.paymentContext.getPlainTagTLV().get(tag)));
			   if (uCubePaymentResponse.paymentContext.getSecuredTagBlock() != null)
			       Log.d(TAG, "secure tag block: " + bytesToHex(uCubePaymentResponse.paymentContext.getSecuredTagBlock()));
		 }
			 }
		});
		} catch (Exception e) {   e.printStackTrace(); }

```

##### Response 

* Several response fields are available when the call back activity is called.
	* paymentContext 
	* uCube
	* cardLabel 

###### PaymentState
```java
		DEFAULT_INIT,
		GET_PN_ERROR,
		GET_MPOS_STATE_ERROR,
		TRANSACTION_MODE_ERROR,
		RISK_MANAGEMENT_TASK_NULL_ERROR,
		AUTHORIZATION_TASK_NULL_ERROR,
		DEVICE_TYPE_ERROR,
		NFC_MPOS_ERROR,
		CARD_WAIT_FAILED,
		CANCELLED,
		STARTED,
		ENTER_SECURE_SESSION,
		CARD_REMOVED,
		CHIP_REQUIRED,
		UNSUPPORTED_CARD,
		TRY_OTHER_INTERFACE,
		REFUSED_CARD,
		ERROR,
		AUTHORIZE,
		APPROVED,
		DECLINED
```

#### checkUpdate  (...)

* This API retrieve the information’s device then the device’s configuration on TMS server. It does a compare, and return a table of required Binary Updates. A binary update can be mandatory or not.

* This API takes those parameters: 
	* ForceUpdate: if true, update the same version will be accept
	* checkOnlyFirmwareVersion: if true, check updates of only firmware(s)

* It can throws an Exception if the initManagers() API not already called.

```java
		try {
			 UCubeAPI.checkUpdate(MainActivity.this,  forceUpdate, checkOnlyFirmwareVersion,
			   new UCubeCheckUpdateListener() {
			       @Override
			       public void onProgress(UCubeAPIState state) { }
			       @Override
			       public void onFinish(boolean status, List<BinaryUpdate> updateList, List<Config> cfgList) {  				//TODO
		}
			});
		} catch (Exception e) {
		   e.printStackTrace();
		}
```

#### update (...)

* This API takes the list of Binary updates to be downloaded and installed.
* It downloads the binary Then it installs them sequentially
* After the install of SVPP firmware the device will reboot. 
* The install of the same version is not accepted by the SVPP.
* The downgrade is not accepted by the SVPP. 
* The forceUpdate of the same version is only accepted with the SVPP firmware is in Security OFF (only available for dev)
* It can throws an Exception if the initManagers() API not already called.

```java
		try {
		   UCubeAPI.update(activity, updateList,
		new UCubeAPIListener() {
			       @Override
			       public void onProgress(UCubeAPIState state) {
			       }
			       @Override
			       public void onFinish(boolean status) {
			       }
			});
		} catch (Exception e) {
		   e.printStackTrace();
		}
```

#### sendLog ()

* uCube SDK manage a logcat that save all RPC exchanges and different user actions. 
* User of SDK can send this logs to be interpreted by the YouTransactor support team.
* It can throw an Exception if the “initManagers” method not already called.

```java
		try {
		   UCubeAPI.sendLogs(MainActivity.this, new UCubeAPIListener() {
		       @Override
		       public void onProgress(UCubeAPIState uCubeAPIState) { }
		       @Override
		       public void onFinish(boolean status) {  }
		   });
		} catch (Exception e) {
		   e.printStackTrace();
		}
```

#### close ()

* This API is used to stop all managers and close connection with uCube.
* It can be called in the onDestroy () method of activity.

### 7. RPC

* This library allows the user to call differents RPC e.g. DisplayMessageWithoutKI, GetInfo, etc.
* User may want to call some RPC, it depends on implementation of one of the tasks “Application Selection Task”, “Risk Management Task” or “Authorization Task”.
* This is an example of DisplayMessageWithoutKI command call: 

```java
		DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand(msg);

		displayMessageCommand.setCentered(centred);
		displayMessageCommand.setYPosition(yPosition);
		displayMessageCommand.setFont(font);

		displayMessageCommand.execute(new ITaskMonitor() {

  			@Override
  			public void handleEvent(TaskEvent event, Object... params) {
     			switch (event) {
     				case FAILED:
     				//TODO
        			break;

     			case SUCCESS:
    				//TODO
        			break;
     			}
  			}
		});
```

![Cptr_logoYT](https://user-images.githubusercontent.com/59020462/71242500-663cdb00-230e-11ea-9a07-3ee5240c6a68.jpeg)
