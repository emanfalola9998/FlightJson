package services

import connectors.SkyHighConnector
import connectors.BlueJetConnector
import mappers.SkyHighMapper
import mappers.BlueJetMapper
import models.Offer

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SearchService @Inject()(
  skyHighConnector: SkyHighConnector,
  blueJetConnector: BlueJetConnector
)(implicit executionContext: ExecutionContext) {

//  def offerings(origin: String, destination: String, date: String) = {
//
//    val skyHighFlights = skyHighConnector.search(origin, destination, date)
//    val blueJetFlights = blueJetConnector.search(origin, destination, date)
//
//    val partitionedResults = for {
//      skyResponse <- skyHighFlights
//      blueResponse <- blueJetFlights
//    } yield {
//      val skyOfferings =
//        skyResponse.map{ sky =>
//          sky.data.flights.map{
//              flight => SkyHighMapper.toOffer(flight)}
//            .map{
//              flight => flight
//            }
//            .partitionMap(identity)
//        }
//
//      val blueOfferings =
//        blueResponse.map{blueFlightsList =>
//          blueFlightsList.map{
//              flight => BlueJetMapper.toOffer(flight)}
//
//                .sortBy(flight => flight)
//                .partitionMap(identity)
//        }
//
//      (skyOfferings, blueOfferings)
//    }
//
//    val validatedOfferings =  partitionedResults.map {
//      case (_, successfulResults) => successfulResults
//    }
//
//    validatedOfferings
//  }




}

object SearchService {

  private[services] def collect(airline: String, result: Either[String, List[Either[String, Offer]]]): (List[Offer], List[String]) = {

    result match {
      case Left(_) =>(List.empty[Offer], List(s"$airline is unavailable"))
      case Right(perFlight) =>
        val (warnings, successfullyParsedFlight) = perFlight.partitionMap(identity) // separates the Right and Lefts into two lists
        (successfullyParsedFlight, warnings)
    }
  }
}
