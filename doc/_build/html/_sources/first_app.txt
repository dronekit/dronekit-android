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

	compile 'com.o3dr:3dr-services-lib:2.1.+'

under the dependencies section.

.. image:: _static/images/hellodrone_setup_5.png

Then click **Sync** in the top right corner to re-sync the gradle.

Connecting to 3DR Services
--------------------------

Implement a ServiceListener on your MainActivity to listen for events sent from 3DR Services to your app.

::

	public class MainActivity extends ActionBarActivity implements ServiceListener {

	    // 3DR Services Listener

	    @Override
	    public void onServiceConnected() {
	        
	    }

	    @Override
	    public void onServiceInterrupted() {
	        
	    }

	    ...
	}


Add a ServiceManager instance to manage the connection.

::

	public class MainActivity extends ActionBarActivity implements ServiceListener {

	    private ServiceManager serviceManager;

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);

	        // Initialize the service manager
	        this.serviceManager = new ServiceManager(getApplicationContext());

	    }

	    @Override
	    public void onStart() {
	        super.onStart();
	        this.serviceManager.connect(this);

	    }

	    @Override
	    public void onStop() {
	        super.onStop();
	        this.serviceManager.disconnect();
	    }

	    // 3DR Services Listener

	    @Override
	    public void onServiceConnected() {

	    }

	    @Override
	    public void onServiceInterrupted() {
	        
	    }

	    ...
	}

Great, now at the start of your Activity, you will initiate a connection to 3DR Services.

Connecting to a drone
--------------------------

Make sure you have your SITL instance running and power up a simulated drone with a UDP output to the IP of your Android device.

For our example, we'll simulate a drone in Berkeley, display the telemetry console and set our output IP to our Android testing device.

In the Linux instance that has SITL installed, go to your terminal.

Navigate to the folder where you cloned the ardupilot repo into.

Enter the following in your console:

::

	sim_vehicle.sh -L 3DRBerkeley --console  --out <ANDROID_DEVICE_IP>:14550


Let's some code in our app to connect to a drone.

First, declare that your MainActivity can act as an interface for DroneListener and implement some methods to listen for drone events.

::

	public class MainActivity extends ActionBarActivity implements DroneListener, ServiceListener {
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

Next, let's add an instance variable to keep track of our drone instance.

::

	public class MainActivity extends ActionBarActivity implements DroneListener, ServiceListener {
		
		private Drone drone;
		private int droneType = Type.TYPE_UNKNOWN;
		private final Handler handler = new Handler();

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);

			this.serviceManager = new ServiceManager(getApplicationContext());
			this.drone = new Drone(this.serviceManager, this.handler);
		}

		@Override
		public void onStart() {
			super.onStart();
			this.serviceManager.connect(this);
		}

		@Override
		public void onStop() {
			super.onStop();
			if (this.drone.isConnected()) {
				this.drone.disconnect();
			}
			this.drone.destroy();
			this.serviceManager.disconnect();
		}


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

