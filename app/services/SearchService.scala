package services

import connectors.SkyHighConnector
import connectors.BlueJetConnector
import mappers.SkyHighMapper
import mappers.BlueJetMapper
import models.{Flight, Offer, SearchResult}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SearchService @Inject()(
  skyHighConnector: SkyHighConnector,
  blueJetConnector: BlueJetConnector
)(implicit executionContext: ExecutionContext) {


  private[services] def skyHighToOffer(origin: String, destination: String, date: String): Future[Either[String, List[Either[String, Offer]]]] = {
    skyHighConnector.search(origin, destination, date)
      .map(either => either
      .map(response => response.data.flights
      .map(flight => SkyHighMapper.toOffer(flight))))
  }

  private[services] def blueJetToOffer(origin: String, destination: String, date: String): Future[Either[String, List[Either[String, Offer]]]] = {
    blueJetConnector.search(origin, destination, date)
      .map(either => either
      .map(listBlueJetResponse => listBlueJetResponse
      .map(blueJetResponse => BlueJetMapper.toOffer(blueJetResponse))))
  }

  def offers(origin: String, destination: String, date: String) = {

    val blueJetOffers = blueJetToOffer(origin, destination, date)
    val skyHighOffers = skyHighToOffer(origin, destination, date)

    for {
      blueJet <- blueJetOffers
      skyHigh <- skyHighOffers
    } yield {

      val partitionedBlueJetOffers =  SearchService.collect("BlueJet", blueJet)
      val partitionedSkyHighOffers = SearchService.collect("SkyHigh", skyHigh)

      val SkyHighOfferResults = SearchResult(partitionedSkyHighOffers._1, partitionedSkyHighOffers._2)
      val blueJetOffersResults = SearchResult(partitionedBlueJetOffers._1, partitionedBlueJetOffers._2)

      (SkyHighOfferResults, blueJetOffersResults)
    }
  }

}

object SearchService {
  private val logger = play.api.Logger(this.getClass)

  private[services] def collect(airline: String, result: Either[String, List[Either[String, Offer]]]): (List[Offer], List[String]) = {

    result match {
      case Left(error) =>
        logger.warn(s"$airline search failed: $error") // error that occurs upstream
        (List.empty[Offer], List(s"$airline is unavailable")) // error comment we create
      case Right(perFlight) =>
        val (warnings, successfullyParsedFlight) = perFlight.partitionMap(identity) // separates the Right and Lefts into two lists
        (successfullyParsedFlight, warnings)
    }
  }
}
