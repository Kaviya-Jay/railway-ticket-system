package com.railwayticketsystem.railwayticketsystem.exception;

import com.railwayticketsystem.railwayticketsystem.exception.QuotaExceededException;
import com.railwayticketsystem.railwayticketsystem.exception.SeatNotAvailableException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle quota limit exceeded
     */
    @ExceptionHandler(QuotaExceededException.class)
    public String handleQuotaExceeded(QuotaExceededException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/search";
    }

    /**
     * Handle seat not available (including optimistic locking failure)
     */
    @ExceptionHandler(SeatNotAvailableException.class)
    public String handleSeatNotAvailable(SeatNotAvailableException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/search";
    }

    /**
     * Handle optimistic locking failure (concurrent booking)
     */
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public String handleOptimisticLockingFailure(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Seat was booked by another user. Please try again.");
        return "redirect:/search";
    }

    /**
     * ✅ NEW: Handle validation & binding errors (prevents Whitelabel for bad date/station input)
     */
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Invalid input. Please check the date and stations.");
        return "redirect:/search";
    }

    /**
     * Generic catch-all for unexpected errors (prevents Whitelabel)
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }
}