package com.akiso.gps_alarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.akiso.gps_alarm.placeholder.PlaceholderContent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var alarmData: AlarmData
    private lateinit var newLocation: LatLng
    private var mapMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->
        mapMarker = googleMap.addMarker(MarkerOptions().position(alarmData.location))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alarmData.location,14.0f))
        googleMap.setOnMapClickListener { tapLocation ->
            mapMarker?.remove()

            newLocation = LatLng(tapLocation.latitude, tapLocation.longitude)
            mapMarker = googleMap.addMarker(MarkerOptions().position(newLocation))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if(!result) {
                    val fragmentManager = parentFragmentManager
                    fragmentManager.popBackStack()
                }
            }
            // ?????????????????????????????????????????????
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val id : Int = (arguments?.get("AlarmID")?:0) as Int
        alarmData = PlaceholderContent.getData(id)?: PlaceholderContent.DEFAULT_DATA
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.memu_map_fragment, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                alarmData.location = newLocation
                findNavController().navigate(R.id.action_mapFragment_to_alarmListFragment)
                return false
            }
        }
        return super.onOptionsItemSelected(item);
    }
}