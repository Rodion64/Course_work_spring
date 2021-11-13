package com.cw.ponomarev.controller;

import com.cw.ponomarev.model.PdfFile;
import com.cw.ponomarev.repos.PdfFileRepo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pr5")
@RequiredArgsConstructor
public class Pr5Controller {

    private final PdfFileRepo pdfFileRepo;

    @GetMapping
    public String mainPage(@CookieValue(value = "translation", required = false) String translation,
                           @CookieValue("backgroundColor") String backgroundColor,
                           @CookieValue("name") String name,
                           Model model, HttpServletResponse response) {
        String translatedHi = "";

        if (translation.equals("ru")) {
            translatedHi = "Здравствуйте, дорогой " + name + "!";
            model.addAttribute("translatedDark", "Темная тема");
            model.addAttribute("translatedLight", "Светлая тема");
            model.addAttribute("translatedRu", "Русский язык");
            model.addAttribute("translatedEn", "Английский язык");
            model.addAttribute("translatedSaveFile", "Добавить");

            model.addAttribute("translatedTitle", "Название файла");
            model.addAttribute("translatedHref", "Ссылка");
            model.addAttribute("translatedDownload", "Скачать");
        } else {
            translatedHi = "Hello, dear " + name + "!";
            model.addAttribute("translatedDark", "Dark theme");
            model.addAttribute("translatedLight", "Light theme");
            model.addAttribute("translatedRu", "Russian language");
            model.addAttribute("translatedEn", "English language");
            model.addAttribute("translatedSaveFile", "Add");

            model.addAttribute("translatedTitle", "File title");
            model.addAttribute("translatedHref", "URL");
            model.addAttribute("translatedDownload", "Download");
        }
        model.addAttribute("translatedHi", translatedHi);
        model.addAttribute("backgroundColor", backgroundColor);
        model.addAttribute("files", pdfFileRepo.findAll());
        return "pr5_main";
    }


    @GetMapping("/dark")
    public String darkTheme(HttpServletRequest request,
                            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        List<Cookie> backgroundColor = Arrays.stream(cookies).filter(c -> c.getName().equals("backgroundColor"))
                .collect(Collectors.toList());
        backgroundColor.get(0).setValue("dark");
        response.addCookie(backgroundColor.get(0));
        return "redirect:/pr5";
    }

    @GetMapping("/light")
    public String lightTheme(HttpServletRequest request,
                             HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        List<Cookie> backgroundColor = Arrays.stream(cookies).filter(c -> c.getName().equals("backgroundColor"))
                .collect(Collectors.toList());
        backgroundColor.get(0).setValue("light");
        response.addCookie(backgroundColor.get(0));
        return "redirect:/pr5";
    }

    @GetMapping("/ru")
    public String ruLang(HttpServletRequest request,
                             HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        List<Cookie> ruLang = Arrays.stream(cookies).filter(c -> c.getName().equals("translation"))
                .collect(Collectors.toList());
        ruLang.get(0).setValue("ru");
        response.addCookie(ruLang.get(0));
        return "redirect:/pr5";
    }

    @GetMapping("/en")
    public String enLang(HttpServletRequest request,
                         HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        List<Cookie> ruLang = Arrays.stream(cookies).filter(c -> c.getName().equals("translation"))
                .collect(Collectors.toList());
        ruLang.get(0).setValue("en");
        response.addCookie(ruLang.get(0));
        return "redirect:/pr5";
    }

    @SneakyThrows
    @PostMapping("/saveFile")
    public String savePDF(@RequestParam MultipartFile[] pdf) {
        for (MultipartFile file : pdf) {
            if(!file.getOriginalFilename().equals("")) {
                PdfFile pdfFile = new PdfFile();
                pdfFile.setFileName(file.getOriginalFilename());
                pdfFile.setType(file.getContentType());
                pdfFile.setPdf(file.getBytes());
                pdfFileRepo.save(pdfFile);
            }
        }
        return "redirect:/pr5";
    }

    @GetMapping("/downloadFile/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<PdfFile> pdfFile = pdfFileRepo.findById(id);
        ByteArrayResource byteArrayResource = new ByteArrayResource(pdfFile.get().getPdf());
        byte[] byteArray = byteArrayResource.getByteArray();

        streamReport(response, byteArray, pdfFile.get());
    }

    protected void streamReport(HttpServletResponse response, byte[] data, PdfFile file)
            throws IOException {

        response.setContentType(file.getType());
        response.setHeader("Content-disposition", "attachment; filename=" + file.getFileName());
        response.setContentLength(data.length);

        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }
}
