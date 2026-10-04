package com.beg.shuttlespot.navigation

sealed class AppDestination(val route: String, val title: String) {
    data object Welcome : AppDestination("welcome", "Welcome")
    data object RoleSelection : AppDestination("role_selection", "Choose Role")
    data object PassengerSignIn : AppDestination("passenger_sign_in", "Student Sign In")
    data object DriverSignIn : AppDestination("driver_sign_in", "Driver Sign In")
    data object LocationPermission : AppDestination("location_permission", "Location Permission")
    data object RouteSelection : AppDestination("route_selection", "Choose Route")
    data object PassengerHomeInactive : AppDestination("passenger_home_inactive", "Home")
    data object PassengerHomeActive : AppDestination("passenger_home_active", "Bus Status")
    data object LiveTrip : AppDestination("live_trip", "Live Trip")
    data object BoardingIntent : AppDestination("boarding_intent", "Boarding Intent")
    data object Announcements : AppDestination("announcements", "Announcements")
    data object Notifications : AppDestination("notifications", "Notifications")
    data object DriverHome : AppDestination("driver_home", "Driver Home")
    data object DriverTrip : AppDestination("driver_trip", "Active Trip")
}
