package ch.heigvd.iict.dma.labo2

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import ch.heigvd.iict.dma.labo2.databinding.ActivityMainBinding
import ch.heigvd.iict.dma.labo2.models.PersistentBeacon
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val beaconsViewModel : BeaconsViewModel by viewModels()

    private val permissionsGranted = MutableLiveData(false)

    private var beaconToNameMap : Map<Int, String> = mapOf(
        46 to "Salle de réunion",
        36 to "Salle de cours",
        23 to "Salle de TP"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check if bluetooth is enabled
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        try {
            if(!bluetoothManager.adapter.isEnabled) {
                Toast.makeText(this, R.string.ble_unavailable, Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (_: java.lang.Exception) { /* getAdapter can launch exception on some smartphone models if permission are not yet granted */ }

        // we request permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBeaconsPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN))
        }
        else {
            requestBeaconsPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        // init views
        val beaconAdapter = BeaconsAdapter()
        binding.beaconsList.adapter = beaconAdapter
        binding.beaconsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // update views
        beaconsViewModel.closestBeacon.observe(this) {beacon ->
            if(beacon != null) {
                binding.location.text = beaconToNameMap[beacon.minor] //beacon.minor.toString()
            } else {
                binding.location.text = getString(R.string.no_beacons)
            }
        }

        beaconsViewModel.nearbyBeacons.observe(this) { nearbyBeacons ->
            if(nearbyBeacons.isNotEmpty()) {
                binding.beaconsList.visibility = View.VISIBLE
                binding.beaconsListEmpty.visibility = View.INVISIBLE
            } else {
                binding.beaconsList.visibility = View.INVISIBLE
                binding.beaconsListEmpty.visibility = View.VISIBLE
            }
            beaconAdapter.items = nearbyBeacons
        }

        val beaconManager =  BeaconManager.getInstanceForApplication(this)
        val region = Region("all-beacons-region", null, null, null)

        beaconManager.beaconParsers.add(
            BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this, rangingObserver)
        beaconManager.startRangingBeacons(region)
    }

    private val requestBeaconsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val isBLEScanGranted =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                    permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false)
                                else
                                    true
        val isFineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val isCoarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

        if (isBLEScanGranted && (isFineLocationGranted || isCoarseLocationGranted) ) {
            // Permission is granted. Continue the action
            permissionsGranted.postValue(true)
        }
        else {
            // Explain to the user that the feature is unavailable
            Toast.makeText(this, R.string.ibeacon_feature_unavailable, Toast.LENGTH_SHORT).show()
            permissionsGranted.postValue(false)
        }
    }

    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("TAG", "Ranged: ${beacons.count()} beacons")
        var beaconList : MutableList<PersistentBeacon> = mutableListOf()

        for (beacon: Beacon in beacons) {
            val pBeacon = PersistentBeacon.convertFromBeacon(beacon)

            if(pBeacon.minor == 46 || pBeacon.minor == 36 || pBeacon.minor == 23)
                beaconList.add(pBeacon)
        }

        beaconList.sortBy {
            it.distance
        }

        beaconsViewModel.setNearbyBeacons(beaconList)

        beaconsViewModel.setPLaceByBeacons(beaconList.firstOrNull())

        for (beacon: PersistentBeacon in beaconList) {
            Log.d("TAG", "$beacon about ${beacon.distance} meters away")
        }
    }

}