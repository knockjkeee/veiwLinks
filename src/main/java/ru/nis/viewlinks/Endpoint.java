package ru.nis.viewlinks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nis.viewlinks.model.File;
import ru.nis.viewlinks.model.Items;
import ru.nis.viewlinks.model.Request;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
public class Endpoint {

    @Value("${web.url}")
    private String url;

    @PostMapping("/links")
    public String getLinks(@RequestBody Items items, Model model) {
        if (items.getItems() == null) {
            model.addAttribute("links", null);
        } else {
            List<File> collect = items.getItems().stream().map(this::prepareData).collect(Collectors.toList());
            model.addAttribute("links", collect);
        }
        return "index";
    }


    private File prepareData(Request request) {
        File file = new File();
        String name = request.getFileName() + request.getExtensionFile();
        String link =
                url + request.getId() + ":~:text=" + request.getFirstName() + "%20" + request.getLastName() + "%20" +
                        request.getPatronymic() + "-," + request.getFileName() + "-," + request.getExtensionFile();
        file.setName(name);
        file.setLink(link);
        return file;
    }
}
