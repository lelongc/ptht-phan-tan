package com.ebook.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ebook.service.EbookService;
import com.ebook.service.OrderService;
import com.ebook.service.VietQRService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final EbookService ebookService;
    private final OrderService orderService;
    private final VietQRService vietQRService;

    @GetMapping("/")
    public String home(Locale locale, Model model) {
        model.addAttribute("ebook", ebookService.getActiveEbook(locale));
        return "index";
    }

    @GetMapping("/payment/{secretCode}")
    public String payment(@PathVariable String secretCode, Model model, Locale locale) {
        var order = orderService.getBySecretCode(secretCode);
        var ebook = ebookService.getActiveEbook(locale);
        String qrUrl = null;
        if (order.getPaymentMethod() != null && order.getPaymentMethod().name().equals("VIETQR")) {
            qrUrl = vietQRService.buildQrImageUrl(order.getAmount(), "ORDER-" + secretCode);
        }
        String paypalUrl = order.getPaymentMethod() != null && order.getPaymentMethod().name().equals("PAYPAL")
                ? "https://www.sandbox.paypal.com/checkoutnow?token=" + secretCode
                : null;
        model.addAttribute("order", order);
        model.addAttribute("ebook", ebook);
        model.addAttribute("secretCode", secretCode);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("paypalUrl", paypalUrl);
        return "payment";
    }

    @GetMapping("/success")
    public String success(@RequestParam String secretCode, Model model, Locale locale) {
        var order = orderService.getBySecretCode(secretCode);
        var ebook = ebookService.getActiveEbook(locale);
        model.addAttribute("order", order);
        model.addAttribute("ebook", ebook);
        model.addAttribute("secretCode", secretCode);
        return "success";
    }
}
