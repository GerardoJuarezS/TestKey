package com.example.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapViewCompose(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(19.4326, -99.1332), // Ciudad de México por defecto
    zoomLevel: Double = 15.0,
    selectedPoint: GeoPoint? = null,
    onMapClick: (GeoPoint) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Configuración necesaria para osmdroid
    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(zoomLevel)
            controller.setCenter(center)
        }
    }

    // Overlay para capturar clics
    val eventsOverlay = remember {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                onMapClick(p)
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        })
    }

    DisposableEffect(mapView) {
        mapView.overlays.add(eventsOverlay)
        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // Actualizar centro si cambia
            if (view.mapCenter.latitude != center.latitude || view.mapCenter.longitude != center.longitude) {
                view.controller.animateTo(center)
            }

            // Actualizar marcador
            view.overlays.removeAll { it is Marker }
            selectedPoint?.let {
                val marker = Marker(view)
                marker.position = it
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Ubicación del Servicio"
                view.overlays.add(marker)
            }
            view.invalidate()
        }
    )
}

@Composable
fun MiniMapView(
    modifier: Modifier = Modifier,
    point: GeoPoint
) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false) // Desactivar gestos para el minimapa
                controller.setZoom(17.0)
                controller.setCenter(point)
                
                val marker = Marker(this)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                this.overlays.add(marker)
            }
        },
        modifier = modifier
    )
}
