package hu.webuni.booking.web;

import hu.webuni.bonus.api.BonusApi;
import hu.webuni.currency.api.CurrencyApi;
import hu.webuni.flights.api.FlightsApi;
import hu.webuni.flights.dto.Airline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import hu.webuni.booking.dto.PurchaseData;
import hu.webuni.booking.dto.TicketData;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Value("${booking.bonus}")
    double bonusRate;

    @Autowired
    BonusApi bonusApi;
    @Autowired
    CurrencyApi currencyApi;
    @Autowired
    FlightsApi flightsApi;

    @PostMapping("/ticket")
    public PurchaseData buyTicket(@RequestBody TicketData ticketData) {
        List<Airline> airlines = flightsApi.searchFlight(ticketData.getFrom(), ticketData.getTo());
        List<Airline> usdAirlines = airlines.stream()
                .map(airline -> {
                    if (!"USD".equals(airline.getCurrency())) {
                        currencyApi.getRate(airline.getCurrency(), "USD");
                        return airline;
                    }
                    return airline;
                })
                .toList();
        PurchaseData purchaseData = new PurchaseData();
        if (airlines.isEmpty()) {
            purchaseData.setSuccess(false);
        } else {
            Optional<Airline> cheapestAirline = usdAirlines.stream().min(Comparator.comparing(Airline::getPrice));
            double userBonus = bonusApi.getPoints(ticketData.getUser());
            double ticketPrice = cheapestAirline.get().getPrice();
            if (ticketData.isUseBonus()) {
                if (ticketPrice > userBonus) {
                    ticketPrice -= userBonus;
                    double bonusEarned = bonusRate * ticketPrice;
                    bonusApi.addPoints(ticketData.getUser(), -userBonus+bonusEarned);
                    purchaseData.setBonusUsed(userBonus);
                    purchaseData.setPrice(ticketPrice);
                    purchaseData.setBonusEarned(bonusEarned);
                } else {
                    bonusApi.addPoints(ticketData.getUser(), -ticketPrice);
                    purchaseData.setBonusUsed(ticketPrice);
                    purchaseData.setPrice(0);
                }
            } else {
                double bonusEarned = bonusRate * ticketPrice;
                bonusApi.addPoints(ticketData.getUser(), bonusEarned);
                purchaseData.setBonusUsed(0);
                purchaseData.setPrice(ticketPrice);
                purchaseData.setBonusEarned(bonusEarned);
            }
            purchaseData.setSuccess(true);
        }
        return purchaseData;
    }
}
