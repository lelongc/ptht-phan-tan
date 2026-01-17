package com.ebook.controller;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ebook.dto.EbookResponse;
import com.ebook.entity.Ebook;
import com.ebook.repository.EbookRepository;
import com.ebook.enums.EbookStatus;
import com.ebook.service.OrderService;
import com.ebook.service.VietQRService;
import com.ebook.service.PriceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final EbookRepository ebookRepository;
    private final OrderService orderService;
    private final VietQRService vietQRService;
    private final MessageSource messageSource;
    private final PriceService priceService;

    @GetMapping("/")
    public String home(Locale locale, Model model) {
        Ebook ebook = ebookRepository.findFirstByStatus(EbookStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active ebook found"));
        
        String title = messageSource.getMessage(ebook.getTitleVi(), null, ebook.getTitleVi(), locale);
        String author = messageSource.getMessage(ebook.getAuthorVi(), null, ebook.getAuthorVi(), locale);
        String description = messageSource.getMessage(ebook.getDescriptionVi(), null, ebook.getDescriptionVi(), locale);
        String price = messageSource.getMessage("ebook.price", null, "10000", locale);
        
        model.addAttribute("ebook", new EbookResponse(
            ebook.getId(),
            title,
            author,
            description,
            new BigDecimal(price),
            ebook.getCoverUrl()
        ));
        return "index";
    }

    @GetMapping("/payment/{secretCode}")
    public String payment(@PathVariable String secretCode, Model model, Locale locale) {
        var order = orderService.getBySecretCode(secretCode);
        
        Ebook ebook = ebookRepository.findFirstByStatus(EbookStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active ebook found"));
        
        String title = messageSource.getMessage(ebook.getTitleVi(), null, ebook.getTitleVi(), locale);
        
        String qrUrl = null;
        if (order.getPaymentMethod() != null && order.getPaymentMethod().name().equals("VIETQR")) {
            qrUrl = vietQRService.buildQrImageUrl(order.getAmount(), "ORDER-" + secretCode);
        }
        String paypalUrl = order.getPaymentMethod() != null && order.getPaymentMethod().name().equals("PAYPAL")
                ? "https://www.sandbox.paypal.com/checkoutnow?token=" + secretCode
                : null;
        model.addAttribute("order", order);
        model.addAttribute("ebookTitle", title);
        model.addAttribute("secretCode", secretCode);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("paypalUrl", paypalUrl);
        return "payment";
    }

    @GetMapping("/success")
    public String success(@RequestParam String secretCode, Model model, Locale locale) {
        var order = orderService.getBySecretCode(secretCode);
        
        Ebook ebook = ebookRepository.findFirstByStatus(EbookStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active ebook found"));
        
        String title = messageSource.getMessage(ebook.getTitleVi(), null, ebook.getTitleVi(), locale);
        
        model.addAttribute("order", order);
        model.addAttribute("ebookTitle", title);
        model.addAttribute("secretCode", secretCode);
        return "success";
    }
}
