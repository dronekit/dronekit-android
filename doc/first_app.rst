First App: Hello Drone
======================

In the first example, we'll walk you through making an app that can connect to a drone, change Ardupilot modes, arm, take off and land the copter.

Project setup
-------------

*Let's set up a basic Android Studio project*

.. image:: _static/images/hellodrone_setup_1.png

*Make sure to use API 15 (Ice Cream Sandwich) or later*

.. image:: _static/images/hellodrone_setup_2.png

*Let's start with a blank activity*

.. image:: _static/images/hellodrone_setup_3.png

*Click finish to create our project*

.. image:: _static/images/hellodrone_setup_4.png

Adding the client library
--------------------------------------

Now open your **build.gradle (Module:app)** file and add: ::

	compile 'com.o3dr:3dr-services-lib:2.2.+'

under the dependencies section.

.. image:: _static/images/hellodrone_setup_5.png

Then click **Sync** in the top right corner to re-sync the gradle.

Connecting to 3DR Services
--------------------------

Implement a TowerListener on your MainActivity to listen for events sent from 3DR Services to your app.

Result:

.. code-block:: java
   :linenos:
   :emphasize-lines: 1,4-12

	public class MainActivity extends ActionBarActivity implements TowerListener {

		// 3DR Services Listener
		@Override
		public void onTowerConnected() {
			
		}

		@Override
		public void onTowerDisconnected() {
			
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
		}
	}


Now we need to:

1. Add a ControlTower instance to manage the communication to 3DR Services.
2. Connect to 3DR Services on start, disconnect on stop of the MainAcvitity

Result:

.. code-block:: java
   :linenos:
   :emphasize-lines: 3,11,18,25,28-36

	public class MainActivity extends ActionBarActivity implements TowerListener {

		private ControlTower controlTower;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);

			// Initialize the service manager
			this.controlTower = new ControlTower(getApplicationContext());

		}

		@Override
		public void onStart() {
			super.onStart();
			this.controlTower.connect(this);

		}

		@Override
		public void onStop() {
			super.onStop();
			this.controlTower.disconnect();
		}

		@Override
		public void onTowerConnected() {

		}

		@Override
		public void onTowerDisconnected() {
			
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);

		}
	}

Connecting to a drone via UDP
-----------------------------

Make sure you have your SITL instance running and power up a simulated drone with a UDP output to the IP of your Android device.

For our example, we'll simulate a drone in Berkeley, display the telemetry console and set our output IP to our Android testing device.

You can find the IP for your Android device in Settings > Wi-Fi. Tap on the connection to get information about it.

In the Linux instance that has SITL installed, go to your terminal.

Navigate to the folder where you cloned the ardupilot repo into.

Enter the following in your console:

::

	sim_vehicle.sh -L 3DRBerkeley --console  --out <ANDROID_DEVICE_IP>:14550



Let's add some code in our app to connect to a drone.

First, declare that your MainActivity can act as an interface for DroneListener and implement some methods to listen for drone events.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-15

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		@Override
		public void onDroneEvent(String event, Bundle extras) {

		}

		@Override
		public void onDroneConnectionFailed(ConnectionResult result) {
			
		}

		@Override
		public void onDroneServiceInterrupted(String errorMsg) {

		}

		...
	}

Next, let's add an instance variable to keep track of our drone instance to the top of our MainActivity.

.. code-block:: java
	:linenos:
	:emphasize-lines: 2-3

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;


The Drone instance will need a generic Android handler to register with the control tower. Let's go ahead and add a handler right where we declare our instance variables.

.. code-block:: java
	:linenos:
	:emphasize-lines: 4

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;
		private final Handler handler = new Handler();


Let's now instantiate a new drone upon the creation of our MainActivity.

.. code-block:: java
	:linenos:
	:emphasize-lines: 7
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.serviceManager = new ServiceManager(getApplicationContext());
		this.drone = new Drone();
	}


Also, let's make sure that when the MainActivity is stopped, we unregister our drone from the control tower. 

.. code-block:: java
	:linenos:
	:emphasize-lines: 4-8
	
	@Override
	public void onStop() {
		super.onStop();
		if (this.drone.isConnected()) {
			this.drone.disconnect();
			updateConnectedButton(false);
		}
                this.controlTower.unregisterDrone(this.drone);
                this.controlTower.disconnect();
	}

Now let's add a button in our activity_main.xml that will connect to a drone on press. Open your **activity_main.xml** file and add the following:

.. code-block:: xml
	:linenos:

	<Button
		android:layout_width="150dp"
		android:layout_height="wrap_content"
		android:text="Connect"
		android:id="@+id/btnConnect"
		android:onClick="onBtnConnectTap"
		android:layout_alignParentRight="true"
		android:layout_alignParentEnd="true" />

Open your **MainActivity** java source and add a method to handle the connect button press:

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	public void onBtnConnectTap(View view) {
		if(this.drone.isConnected()) {
			this.drone.disconnect();
		} else {
			Bundle extraParams = new Bundle();
			extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, 14550); // Set default port to 14550

			ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_UDP, extraParams, null);
			this.drone.connect(connectionParams);
		}
	}

Let's see what's going on in the above method.

First if we are connected, then use this button to disconnect.

If we are not connected, we need to build a set of connection parameters and connect.

Now let's add som UI to alert us when the drone is connected.

Add the following UI helper method to the bottom of your MainActivity file.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	protected void alertUser(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	protected void updateConnectedButton(Boolean isConnected) {
		Button connectButton = (Button)findViewById(R.id.btnConnect);
		if (isConnected) {
			connectButton.setText("Disconnect");
		} else {
			connectButton.setText("Connect");
		}
	}

Let's revisit the **onDroneEvent** method to alert the user when the drone is connected.

Add the following to your **onDroneEvent** method:

.. code-block:: java
	:linenos:
	:emphasize-lines: 3-16

	@Override
	public void onDroneEvent(String event, Bundle extras) {
		switch (event) {
			case AttributeEvent.STATE_CONNECTED:
				alertUser("Drone Connected");
				updateConnectedButton(this.drone.isConnected());
				break;

			case AttributeEvent.STATE_DISCONNECTED:
				alertUser("Drone Disconnected");
				updateConnectedButton(this.drone.isConnected());
				break;

			default:
				break;
		}
	}

Now if you run the app and the SITL environment, you should be able to connect to your drone!

Connecting via USB (3DR Telemetry Radio)
----------------------------------------

For USB connections, you need to define an extra param for the baud rate.

Example:

.. code-block:: java
	:linenos:

	Bundle extraParams = new Bundle();
	extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, 57600); // Set default baud rate to 57600
	ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_USB, extraParams, null);
	this.drone.connect(connectionParams);

Getting telemetry from your drone
---------------------------------

In order to get telemetry updates from the drone, we need to add cases for different drone events returned in **onDroneEvent**.

Update your **onDroneEvent** to look like the following:

.. code-block:: java
	:linenos:
	:emphasize-lines: 14-34

	@Override
	public void onDroneEvent(String event, Bundle extras) {
		switch (event) {
			case AttributeEvent.STATE_CONNECTED:
				alertUser("Drone Connected");
				updateConnectedButton(this.drone.isConnected());
				break;

			case AttributeEvent.STATE_DISCONNECTED:
				alertUser("Drone Disconnected");
				updateConnectedButton(this.drone.isConnected());
				break;

			case AttributeEvent.STATE_VEHICLE_MODE:
				updateVehicleMode();
				break;

			case AttributeEvent.TYPE_UPDATED:
				Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
				if (newDroneType.getDroneType() != this.droneType) {
					this.droneType = newDroneType.getDroneType();
					updateVehicleModesForType(this.droneType);
				}
				break;


			case AttributeEvent.SPEED_UPDATED:
				updateAltitude();
				updateSpeed();
				break;

			case AttributeEvent.HOME_UPDATED:
				updateDistanceFromHome();
				break;

			default:
				break;
		}
	}

Let's add some TextViews to our UI in order to output telemetry values. In your **activity_main.xml** add the following:

.. code-block:: xml

	<TableLayout
		android:layout_width="fill_parent"
		android:layout_height="200dp"
		android:layout_below="@+id/telemetryLabel"
		android:layout_alignParentLeft="true"
		android:layout_alignParentStart="true"
		android:layout_marginTop="10dp">

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow1">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Mode:"
				android:id="@+id/vehicleModeLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<Spinner
				android:layout_width="fill_parent"
				android:layout_height="44dp"
				android:id="@+id/modeSelect"
				android:spinnerMode="dropdown"
				android:layout_below="@+id/connectionTypeLabel"
				android:layout_toLeftOf="@+id/btnConnect"
				android:layout_alignParentLeft="true"
				android:layout_alignParentStart="true"
				android:layout_column="1" />
		</TableRow>

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow2">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Altitude:"
				android:id="@+id/altitudeLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<TextView
				android:layout_width="fill_parent"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="0m"
				android:id="@+id/altitudeValueTextView"
				android:layout_column="1"
				android:paddingTop="5dp"
				android:paddingBottom="5dp"
				android:layout_gravity="left" />
		</TableRow>

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow3">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Speed:"
				android:id="@+id/speedLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<TextView
				android:layout_width="fill_parent"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="0m/s"
				android:id="@+id/speedValueTextView"
				android:layout_column="1"
				android:paddingTop="5dp"
				android:paddingBottom="5dp"
				android:layout_gravity="left" />
		</TableRow>

		<TableRow
			android:layout_width="fill_parent"
			android:layout_height="fill_parent"
			android:id="@+id/vehTelemRow4">

			<TextView
				android:layout_width="100dp"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="Distance:"
				android:id="@+id/distanceLabelTextView"
				android:layout_column="0"
				android:paddingTop="5dp"
				android:paddingBottom="5dp" />

			<TextView
				android:layout_width="fill_parent"
				android:layout_height="wrap_content"
				android:textAppearance="?android:attr/textAppearanceMedium"
				android:text="0m"
				android:id="@+id/distanceValueTextView"
				android:layout_column="1"
				android:paddingTop="5dp"
				android:paddingBottom="5dp"
				android:layout_gravity="left" />
		</TableRow>

	</TableLayout>

In the above layout code, we are adding a table with TextViews and a Spinner Dropdown view that will let us change our vehicle's modes.

Also add a class level Spinner variable in MainActivity so we can reference it throughout the code.

.. code-block:: java
	:linenos:
	:emphasize-lines: 5

	public class MainActivity extends ActionBarActivity implements DroneListener, TowerListener {
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;
		private final Handler handler = new Handler();
		Spinner modeSelector;

And add to our **onCreate** method to reference the Spinner defined in the XML layout:

.. code-block:: java
	:linenos:
	:emphasize-lines: 10-20

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Context context = getApplicationContext();
		this.controlTower = new ControlTower(context);
		this.drone = new Drone();

		this.modeSelector = (Spinner)findViewById(R.id.modeSelect);
		this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				onFlightModeSelected(view);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});
	}

Now let's implement some of the methods in our **onDroneEvent** in order to update our UI. Add the following methods to your MainActivity.

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	public void onFlightModeSelected(View view) {
		VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();
		this.drone.changeVehicleMode(vehicleMode);
	}

	protected void updateVehicleModesForType(int droneType) {
		List<VehicleMode> vehicleModes =  VehicleMode.getVehicleModePerDroneType(droneType);
		ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
		vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.modeSelector.setAdapter(vehicleModeArrayAdapter);
	}

	protected void updateVehicleMode() {
		State vehicleState = this.drone.getAttribute(AttributeType.STATE);
		VehicleMode vehicleMode = vehicleState.getVehicleMode();
		ArrayAdapter arrayAdapter = (ArrayAdapter)this.modeSelector.getAdapter();
		this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
	}

	protected void updateAltitude() {
		TextView altitudeTextView = (TextView)findViewById(R.id.altitudeValueTextView);
		Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
		altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
	}

	protected void updateSpeed() {
		TextView speedTextView = (TextView)findViewById(R.id.speedValueTextView);
		Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
		speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
	}

	protected void updateDistanceFromHome() {
		TextView distanceTextView = (TextView)findViewById(R.id.distanceValueTextView);
		Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
		double vehicleAltitude = droneAltitude.getAltitude();
		Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
		LatLong vehiclePosition = droneGps.getPosition();

		double distanceFromHome =  0;

		if (droneGps.isValid()) {
			LatLongAlt vehicle3DPosition = new LatLongAlt(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
			Home droneHome = this.drone.getAttribute(AttributeType.HOME);
			distanceFromHome = distanceBetweenPoints(droneHome.getCoordinate(), vehicle3DPosition);
		} else {
			distanceFromHome = 0;
		}

		distanceTextView.setText(String.format("%3.1f", distanceFromHome) + "m");
	}

	protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB) {
		if (pointA == null || pointB == null) {
			return 0;
		}
		double dx = pointA.getLatitude() - pointB.getLatitude();
		double dy  = pointA.getLongitude() - pointB.getLongitude();
		double dz = pointA.getAltitude() - pointB.getAltitude();
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

Whoa! A lot of stuff! Let's go through it together:

::

	public void onFlightModeSelected(View view)

This changes the mode of the vehicle when the user changes the selection of the mode selector.

::

	protected void updateVehicleModesForType(int droneType)

This is triggered when the **onDroneEvent** tells us the type of vehicle we're dealing with. In the **onDroneEvent**, we get the type of vehicle and load the modes the the vehicle can have.

::

	// Fired when the vehicle mode changes on the drone.
	protected void updateVehicleMode()


::

	// Fired when the altitude of the drone updates.
	protected void updateAltitude()


::

	// Fired when the speed of the drone updates.
	protected void updateSpeed()


::

	// A convenience method for calculating the distance between two 3D points.
	protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB)


Take Off!
---------

Let's add a button to our app that will allow us to arm, take off and land the vehicle.

.. code-block:: xml

	<Button
		android:layout_width="120dp"
		android:layout_height="wrap_content"
		android:id="@+id/btnArmTakeOff"
		android:layout_alignParentRight="true"
		android:layout_alignParentEnd="true"
		android:layout_column="1"
		android:visibility="invisible"
		android:onClick="onArmButtonTap" />

Add a method to our MainActivity to update our button's UI depending on the vehicle state:

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-100

	protected void updateArmButton() {
		State vehicleState = this.drone.getAttribute(AttributeType.STATE);
		Button armButton = (Button)findViewById(R.id.btnArmTakeOff);

		if (!this.drone.isConnected()) {
			armButton.setVisibility(View.INVISIBLE);
		} else {
			armButton.setVisibility(View.VISIBLE);
		}

		if (vehicleState.isFlying()) {
			// Land
			armButton.setText("LAND");
		} else if (vehicleState.isArmed()) {
			// Take off
			armButton.setText("TAKE OFF");
		} else if (vehicleState.isConnected()){
			// Connected but not Armed
			armButton.setText("ARM");
		}
	}

Add a method to our MainActivity to handle the arm button press:

.. code-block:: java
	:linenos:
	:emphasize-lines: 1-25

	public void onArmButtonTap(View view) {
		Button thisButton = (Button)view;
		State vehicleState = this.drone.getAttribute(AttributeType.STATE);

		if (vehicleState.isFlying()) {
			// Land
			this.drone.changeVehicleMode(VehicleMode.COPTER_LAND);
		} else if (vehicleState.isArmed()) {
			// Take off
			this.drone.doGuidedTakeoff(10); // Default take off altitude is 10m
		} else if (!vehicleState.isConnected()) {
			// Connect
			alertUser("Connect to a drone first");
		} else if (vehicleState.isConnected() && !vehicleState.isArmed()){
			// Connected but not Armed
			this.drone.arm(true);
		}
	}

Let's go back to our good old **onDroneEvent** to link updating our arm button UI to the drone events:

.. code-block:: java
	:linenos:
	:emphasize-lines: 18-21

	@Override
	public void onDroneEvent(String event, Bundle extras) {

		switch (event) {
			case AttributeEvent.STATE_CONNECTED:
				alertUser("Drone Connected");
				updateConnectedButton(this.drone.isConnected());
				updateArmButton();

				break;

			case AttributeEvent.STATE_DISCONNECTED:
				alertUser("Drone Disconnected");
				updateConnectedButton(this.drone.isConnected());
				updateArmButton();
				break;

			case AttributeEvent.STATE_UPDATED:
			case AttributeEvent.STATE_ARMING:
				updateArmButton();
				break;

			case AttributeEvent.TYPE_UPDATED:
				Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
				if (newDroneType.getDroneType() != this.droneType) {
					this.droneType = newDroneType.getDroneType();
					updateVehicleModesForType(this.droneType);
				}
				break;

			case AttributeEvent.STATE_VEHICLE_MODE:
				updateVehicleMode();
				break;


			case AttributeEvent.SPEED_UPDATED:
				updateAltitude();
				updateSpeed();
				break;

			case AttributeEvent.HOME_UPDATED:
				updateDistanceFromHome();
				break;
			default:
				 Log.i("DRONE_EVENT", event);
				break;
		}
	}

Now run the app and SITL and you should be able to connect, arm, and take off!

Summary
-------

Congratulations! You've just made your first drone app. You can find the full source code for this example on `Github <https://github.com/3drobotics/Android-DroneAPI-starter>`_.




