package ro.hacktm.cashacab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import ro.hacktm.cashacab.PrinterServer;
import ro.hacktm.cashacab.PrinterServerListener;
import ro.hacktm.cashacab.R;
import ro.hacktm.cashacab.R.id;
import ro.hacktm.cashacab.R.layout;
import ro.hacktm.cashacab.R.menu;
import ro.hacktm.cashacab.R.string;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.datecs.api.printer.Printer;
import com.datecs.api.printer.PrinterInformation;
import com.datecs.api.printer.ProtocolAdapter;

public class MainActivity extends ActionBarActivity {
	TextView nameCabbie, idCabbie, currentPrice, distanceDriven, startPrice, idlePrice, distancePrice;
	Switch tariffTypeSwitch;
	Button startButton, stopButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionBar =getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(
				Color.parseColor("#ffe02f"));
		actionBar.setBackgroundDrawable(colorDrawable);
//		mRestart = true;
//        waitForConnection();
        
		currentPrice = (TextView) findViewById(R.id.currentPrice);
		idCabbie = (TextView) findViewById(R.id.idCabbie);
		nameCabbie = (TextView) findViewById(R.id.nameCabbie);
		distanceDriven = (TextView) findViewById(R.id.distanceDriven);
		startPrice = (TextView) findViewById(R.id.startPrice);
		idlePrice = (TextView) findViewById(R.id.idlePrice);
		distancePrice = (TextView) findViewById(R.id.distancePrice);
		
		tariffTypeSwitch = (Switch) findViewById(R.id.switchTariff);
		
		startButton = (Button) findViewById(R.id.buttonStart);
		stopButton = (Button) findViewById(R.id.buttonStop);
		
		stopButton.setEnabled(false);
		
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopButton.setEnabled(true);
				startButton.setEnabled(false);
				
			}
		});
		
		stopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				
				//printText();
				
			}
		});
		
		tariffTypeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			 
			   @Override
			   public void onCheckedChanged(CompoundButton buttonView,
			     boolean isChecked) {
			 
			    if(isChecked){
			    	 startPrice.setText("Start price: 2.89 LEI");
					   idlePrice.setText("Idling price: 28.90 LEI/H");
					   distancePrice.setText("Distance price: 2.89 LEI");
			    }else{
			    	startPrice.setText("Start price: 2.19 LEI");
					  idlePrice.setText("Idling price: 21.90 LEI/H");
					  distancePrice.setText("Distance price: 2.19 LEI");
			    }
			 
			   }
			  });
			   
			  //check the current state before we display the screen
			  if(tariffTypeSwitch.isChecked()){
			   startPrice.setText("Start price: 2.89 LEI");
			   idlePrice.setText("Idling price: 28.90 LEI/H");
			   distancePrice.setText("Distance price: 2.89 LEI");
			  }
			  else {
				  startPrice.setText("Start price: 2.19 LEI");
				  idlePrice.setText("Idling price: 21.90 LEI/H");
				  distancePrice.setText("Distance price: 2.19 LEI");
				  
			  }
			 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_history) {
			Intent i = new Intent(MainActivity.this, HistoryActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
    @Override
	protected void onDestroy() {
        super.onDestroy();
        mRestart = false;               
        closeActiveConnection();
	}	
		    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GET_DEVICE) {
            if (resultCode == DeviceListActivity.RESULT_OK) {   
            	String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            	//address = "192.168.11.136:9100";
            	if (BluetoothAdapter.checkBluetoothAddress(address)) {
            		establishBluetoothConnection(address);
            	} else {
            		establishNetworkConnection(address);
            	}
            } else if (resultCode == RESULT_CANCELED) {
                
            } else {
                finish();
            }
        }
    }
    
	// Member variables
	private Printer mPrinter;
	private ProtocolAdapter mProtocolAdapter;
	private PrinterInformation mPrinterInfo;
	private BluetoothSocket mBluetoothSocket;
	private PrinterServer mPrinterServer;
	private Socket mPrinterSocket;
	private boolean mRestart;
	
    // Debug
    private static final String LOG_TAG = "PrinterSample"; 
    private static final boolean DEBUG = true;
    
    // Request to get the bluetooth device
    private static final int REQUEST_GET_DEVICE = 0; 
    
    // Request to get the bluetooth device
    private static final int DEFAULT_NETWORK_PORT = 9100; 
        
	// The listener for all printer events
	private final ProtocolAdapter.ChannelListener mChannelListener = new ProtocolAdapter.ChannelListener() {
        @Override
        public void onReadEncryptedCard() {
            toast(getString(R.string.msg_read_encrypted_card));
        }
        
        @Override
        public void onPaperReady(boolean state) {
            if (state) {
                toast(getString(R.string.msg_paper_ready));
            } else {
                toast(getString(R.string.msg_no_paper));
            }
        }
        
        @Override
        public void onOverHeated(boolean state) {
            if (state) {
                toast(getString(R.string.msg_overheated));
            }
        }
               
        @Override
        public void onLowBattery(boolean state) {
            if (state) {
                toast(getString(R.string.msg_low_battery));
            }
        }

		@Override
		public void onReadBarcode() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReadCard() {
			// TODO Auto-generated method stub
			
		}
    };      
    
    private void toast(final String text) {
        runOnUiThread(new Runnable() {            
            @Override
            public void run() {
                if (!isFinishing()) {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void error(final String text, boolean resetConnection) {        
        if (resetConnection) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {        
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();                
                }           
            });
                
            waitForConnection();
        }
    }
    
    private void doJob(final Runnable job, final int resId) {
        // Start the job from main thread
        runOnUiThread(new Runnable() {            
            @Override
            public void run() {
                // Progress dialog available due job execution
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle(getString(R.string.title_please_wait));
                dialog.setMessage(getString(resId));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                
                Thread t = new Thread(new Runnable() {            
                    @Override
                    public void run() {                
                        try {
                            job.run();
                        } finally {
                            dialog.dismiss();
                        }
                    }
                });
                t.start();   
            }
        });                     
    }
    
    protected void initPrinter(InputStream inputStream, OutputStream outputStream) throws IOException {
        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);
       
        if (mProtocolAdapter.isProtocolEnabled()) {
            final ProtocolAdapter.Channel channel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            channel.setListener(mChannelListener);
            // Create new event pulling thread
            new Thread(new Runnable() {                
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                        try {
                            channel.pullEvent();
                        } catch (IOException e) {
                        	e.printStackTrace();
                            error(e.getMessage(), mRestart);
                            break;
                        }
                    }
                }
            }).start();
            mPrinter = new Printer(channel.getInputStream(), channel.getOutputStream());
        } else {
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
        }
        
        mPrinterInfo = mPrinter.getInformation();
        
//        runOnUiThread(new Runnable() {          
//            @Override
//            public void run() {
//                ((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.icon);
//                ((TextView)findViewById(R.id.name)).setText(mPrinterInfo.getName());
//            }
//        });
    }
    
    public synchronized void waitForConnection() {
        closeActiveConnection();
        
        // Show dialog to select a Bluetooth device. 
        startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_GET_DEVICE);
        
        // Start server to listen for network connection.
        try {
            mPrinterServer = new PrinterServer(new PrinterServerListener() {                
                @Override
                public void onConnect(Socket socket) {
                    if (DEBUG) Log.d(LOG_TAG, "Accept connection from " + socket.getRemoteSocketAddress().toString());
                    
                    // Close Bluetooth selection dialog
                    finishActivity(REQUEST_GET_DEVICE);                    
                    
                    mPrinterSocket = socket;
                    try {
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();
                        initPrinter(in, out);
                    } catch (IOException e) {   
                    	e.printStackTrace();
                        error(getString(R.string.msg_failed_to_init) + ". " + e.getMessage(), mRestart);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   
    
    private void establishBluetoothConnection(final String address) {
    	closePrinterServer();
        
        doJob(new Runnable() {           
            @Override
            public void run() {      
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();                
                BluetoothDevice device = adapter.getRemoteDevice(address);                    
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                InputStream in = null;
                OutputStream out = null;
                
                adapter.cancelDiscovery();
                
                try {
                    if (DEBUG) Log.d(LOG_TAG, "Connect to " + device.getName());
                    mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                    mBluetoothSocket.connect();
                    in = mBluetoothSocket.getInputStream();
                    out = mBluetoothSocket.getOutputStream();                                        
                } catch (IOException e) {    
                	e.printStackTrace();
                    error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
                }                                  
                
                try {
                    initPrinter(in, out);
                    toast("Imprimanta conectata");
                } catch (IOException e) {
                	e.printStackTrace();
                    error(getString(R.string.msg_failed_to_init) + ". " +  e.getMessage(), mRestart);
                    return;
                }
            }
        }, R.string.msg_connecting); 
        
        
    }
    
    private void establishNetworkConnection(final String address) {
    	closePrinterServer();
        
        doJob(new Runnable() {           
            @Override
            public void run() {            	
            	Socket s = null;
            	try {
            		String[] url = address.split(":");
            		int port = DEFAULT_NETWORK_PORT;
            		
            		try {
            			if (url.length > 1)  {
            				port = Integer.parseInt(url[1]);
            			}
            		} catch (NumberFormatException e) { }
            		
            		s = new Socket(url[0], port);
            		s.setKeepAlive(true);
                    s.setTcpNoDelay(true);
	            } catch (UnknownHostException e) {
	            	error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
	            } catch (IOException e) {
	            	error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
	            }            
            	
                InputStream in = null;
                OutputStream out = null;
                
                try {
                    if (DEBUG) Log.d(LOG_TAG, "Connect to " + address);
                    mPrinterSocket = s;                    
                    in = mPrinterSocket.getInputStream();
                    out = mPrinterSocket.getOutputStream();                                        
                } catch (IOException e) {                    
                    error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
                }                                  
                
                try {
                    initPrinter(in, out);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_init) + ". " +  e.getMessage(), mRestart);
                    return;
                }
            }
        }, R.string.msg_connecting); 
    }
    
    private synchronized void closeBlutoothConnection() {        
        // Close Bluetooth connection 
        BluetoothSocket s = mBluetoothSocket;
        mBluetoothSocket = null;
        if (s != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Blutooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }        
    }
    
    private synchronized void closeNetworkConnection() {
        // Close network connection
        Socket s = mPrinterSocket;
        mPrinterSocket = null;
        if (s != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Network socket");
            try {
                s.shutdownInput();
                s.shutdownOutput();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }            
        }
    }
    
    private synchronized void closePrinterServer() {
    	closeNetworkConnection();
    	
        // Close network server
        PrinterServer ps = mPrinterServer;
        mPrinterServer = null;
        if (ps != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Network server");
            try {
                ps.close();
            } catch (IOException e) {                
                e.printStackTrace();
            }            
        }     
    }
    
    private synchronized void closePrinterConnection() {
        if (mPrinter != null) {
            mPrinter.release();
        }
        
        if (mProtocolAdapter != null) {
            mProtocolAdapter.release();
        }
    }
    
    private synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBlutoothConnection();
        closeNetworkConnection();  
        closePrinterServer();
    }
    
    private void printText() {
	    doJob(new Runnable() {           
            @Override
            public void run() {
        		StringBuffer sb = new StringBuffer();
//        		sb.append("{reset}{center}{w}{h}HackTM 2014{br}");
//        		sb.append("{reset}{center}{w}{h}PFA " + nameCabbie.getText().toString()+"{br}");
//        		sb.append("{reset}{center}TAXI");
//                sb.append("{br}");
//                sb.append("{br}");
//                sb.append("{reset}TM 21 ERP{br}");
//                sb.append("{reset}"+startPrice.getText().toString()+"{br}");
//                sb.append("{reset}"+idlePrice.getText().toString()+"{br}");
//                sb.append("{reset}Idle time: 00:02:34        0.94 LEI{br}");
//                sb.append("{reset}"+distancePrice.getText().toString()+"{br}");
//                sb.append("{reset}KM:"+distanceDriven.getText().toString()+"        6.39 LEI{br}");
//                sb.append("{br}");
//                sb.append("{reset}{right}{w}{h}TOTAL: {/w}9.52 LEI  {br}");            
//                sb.append("{br}");
            	
        		sb.append("{reset}{center}{w}{h}HackTM 2014{br}{br}{br}");
        		sb.append("{reset}{center}{w}{h}Greetings from team {br}");
        		sb.append("{reset}{center}{w}{h}ECHIPA{br}");
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        		String newDate = formatter.format(Calendar.getInstance().getTime());
                
                sb.append("{reset}"+newDate+"{br}");
                sb.append("{reset}{center}{s}Thank You!{br}");
                
            	try {   
            	    if (DEBUG) Log.d(LOG_TAG, "Print Text");
            		mPrinter.reset();            		
                    mPrinter.printTaggedText(sb.toString());                    
                    mPrinter.feedPaper(110); 
                    mPrinter.flush();                                          		
            	} catch (IOException e) {
            		e.printStackTrace();
            	    error(getString(R.string.msg_failed_to_print_text) + ". " + e.getMessage(), mRestart);    		
            	}
            }
	    }, R.string.msg_printing_text);
	}
}
