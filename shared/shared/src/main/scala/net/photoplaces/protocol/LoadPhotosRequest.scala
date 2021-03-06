package net.photoplaces.protocol

case class LoadPhotosRequest(latitude: Double, longitude: Double)
case class LoadPhotosResponse(photos: List[Photo])
