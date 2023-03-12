package ru.nis.viewlinks;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.nis.viewlinks.model.FileLink;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api")
public class Endpoint {

    private final RestTemplate restTemplate;
    @Value("${web.url}")
    private String baseUrl;

    private final String ERROR_500 = "Переход не может быть выполнен: Время жизни ключа авторизации истекло. Перезагрузите основное обращение";

    public Endpoint(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    @GetMapping("/links/{root}/{files}/{names}")
    public String getLinks(Model model, @PathVariable Map<String, String> encode) {
        String accessKey = encode.get("root");
        String[] files = encode.get("files").split(",");
        String[] names = encode.get("names").split(",");

        if (files[0].equals("empty")) {
            return "index";
        }
        List<FileLink> collect = prepareLinks(accessKey, files, names);
        model.addAttribute("links", collect);
        return "index";
    }

    @SneakyThrows
    @GetMapping(value = {"/jpeg/view/{encode}", "/jpg/view/{encode}"})
    public String getViewJPEG(Model model, @PathVariable String encode) {
        String src = "/api/jpeg/" + encode;
        String[] data = getData(encode);
        byte[] fileContent = getBytes(data);
        if (fileContent.length == 0 ){
            model.addAttribute("error", ERROR_500);
            return "viewImage";
        }
        model.addAttribute("src", src);
        model.addAttribute("type", "image/jpeg");
        return "viewImage";
    }

    @SneakyThrows
    @GetMapping("/png/view/{encode}")
    public String getViewPNG(Model model, @PathVariable String encode) {
        String src = "/api/png/" + encode;

        String[] data = getData(encode);
        byte[] fileContent = getBytes(data);
        if (fileContent.length == 0 ){
            model.addAttribute("error", ERROR_500);
            return "viewImage";
        }

        model.addAttribute("src", src);
        model.addAttribute("type", "image/png");
        return "viewImage";
    }




    @SneakyThrows
    @GetMapping("/pdf/{encode}")
    public ResponseEntity<byte[]> getPDF(@PathVariable String encode) {
        String[] data = getData(encode);
        byte[] fileContent = getBytes(data);
        HttpHeaders headers = new HttpHeaders();

        if(fileContent.length == 0){
            headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
            return new ResponseEntity<byte[]>(ERROR_500.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }

        String fileName = data[2];
        headers.setContentType(MediaType.parseMediaType("application/pdf; charset=UTF-8"));
        headers.add("content-disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping(value = {"/jpeg/{encode}", "/jpg/{encode}"})
    public ResponseEntity<byte[]> getJPEG(@PathVariable String encode) {
        String[] data = getData(encode);
        byte[] fileContent = getBytes(data);
        HttpHeaders headers = new HttpHeaders();

        if(fileContent.length == 0){
            headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
            return new ResponseEntity<byte[]>(ERROR_500.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }

        String fileName = data[2];
        headers.setContentType(MediaType.parseMediaType("image/jpeg; charset=UTF-8"));
        headers.add("content-disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }


    @SneakyThrows
    @GetMapping("/png/{encode}")
    public ResponseEntity<byte[]> getPNG(@PathVariable String encode) {
        String[] data = getData(encode);
        byte[] fileContent = getBytes(data);
        HttpHeaders headers = new HttpHeaders();

        if(fileContent.length == 0){
            headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
            return new ResponseEntity<byte[]>(ERROR_500.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }

        String fileName = data[2];
        headers.setContentType(MediaType.parseMediaType("image/png; charset=UTF-8"));
        headers.add("content-disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @GetMapping("/health")
    @ResponseBody
    public String test() {
        return "I am is alive";
    }

    private String[] getData(@PathVariable String encode) {
        String attr = new String(Base64.getUrlDecoder().decode(encode));
        return attr.split("/");
    }

    private List<FileLink> prepareLinks(String accessKey, String[] files, String[] names) {
        List<FileLink> collect = IntStream.range(0, files.length).mapToObj(index -> {
            String link = null;
            if (names[index].endsWith(".pdf")) {
                link = accessKey + "/" + files[index] + "/" + names[index];
                link = "/api/pdf/" + Base64.getUrlEncoder().encodeToString(link.getBytes());
            }
            if (names[index].endsWith(".jpeg")) {
                link = accessKey + "/" + files[index] + "/" + names[index];
                link = "/api/jpeg/view/" + Base64.getUrlEncoder().encodeToString(link.getBytes());
            }
            if (names[index].endsWith(".jpg")) {
                link = accessKey + "/" + files[index] + "/" + names[index];
                link = "/api/jpg/view/" + Base64.getUrlEncoder().encodeToString(link.getBytes());
            }
            if (names[index].endsWith(".png")) {
                link = accessKey + "/" + files[index] + "/" + names[index];
                link = "/api/png/view/" + Base64.getUrlEncoder().encodeToString(link.getBytes());

            }

            return new FileLink(names[index], link);
        }).collect(Collectors.toList());
        return collect;
    }

    private byte[] getBytes(String[] data) {
        String accessKey = data[0];
        String fileUUID = data[1];
        String url = baseUrl + fileUUID + "?accessKey=" + accessKey;
        byte[] forObject = new byte[0];
        try {
            forObject = restTemplate.getForObject(url, byte[].class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return forObject;
    }
}
