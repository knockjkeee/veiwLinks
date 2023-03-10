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
import org.springframework.web.client.RestTemplate;
import ru.nis.viewlinks.model.FileLink;
import ru.nis.viewlinks.model.Request;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/api")
public class Endpoint {

    private final RestTemplate restTemplate;
    @Value("${web.url}")
    private String baseUrl;

    public Endpoint(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/links/{accessKey}/{appeal}/{files}/{names}")
    public String getLinks(Model model, @PathVariable Map<String, String> pathVarsMap) {
        String accessKey = pathVarsMap.get("accessKey");
        String appeal = pathVarsMap.get("appeal");
        String[] files = pathVarsMap.get("files").split(",");
        String[] names = pathVarsMap.get("names").split(",");

        if (files[0].equals("empty")) {
            return "index";
        }

        List<FileLink> collect = IntStream.range(0, files.length).mapToObj(index -> {
            String link = null;
            if (names[index].endsWith(".pdf")) {
                link = "/api/pdf/" + accessKey + "/" + files[index] + "/" + names[index];
            }
            if (names[index].endsWith(".jpeg")) {
                link = "/api/jpeg/" + accessKey + "/" + files[index] + "/" + names[index];
            }
            if (names[index].endsWith(".png")) {
                link = "/api/png/" + accessKey + "/" + files[index] + "/" + names[index];
            }
            return new FileLink(names[index], link);
        }).collect(Collectors.toList());

        model.addAttribute("links", collect);
        return "index";
    }

    @GetMapping("/health")
    @ResponseBody
    public String test() {
        return "I am is alive";
    }

//    http://localhost/api/links/11a51ee0-47b5-4808-a2b2-d4383d2583e6/appeal$7539601/file$7544305,file$7544306,file$7544307/test.jpeg,150944953.pdf,%D0%A1%D1%85%D0%B5%D0%BC%D0%B0.png

    @SneakyThrows
    @GetMapping("/pdf/{accessKey}/{fileUUID}/{fileName}")
    public ResponseEntity<byte[]> getPDF(@PathVariable Map<String, String> pathVarsMap) {

        byte[] fileContent = getBytes(pathVarsMap);
        String fileName = pathVarsMap.get("fileName");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf; charset=UTF-8"));
        headers.add("content-disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping("/jpeg/{accessKey}/{fileUUID}/{fileName}")
    public ResponseEntity<byte[]> getJPEG(@PathVariable Map<String, String> pathVarsMap) {

        byte[] fileContent = getBytes(pathVarsMap);
        String fileName = pathVarsMap.get("fileName");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/jpeg; charset=UTF-8"));
        headers.add("content-disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    private byte[] getBytes(Map<String, String> pathVarsMap) {
        String accessKey = pathVarsMap.get("accessKey");
        String fileUUID = pathVarsMap.get("fileUUID");
        String url = baseUrl + fileUUID + "?accessKey=" + accessKey;
        return restTemplate.getForObject(url, byte[].class);
    }

    @SneakyThrows
    @GetMapping("/png/{accessKey}/{fileUUID}/{fileName}")
    public ResponseEntity<byte[]> getPNG(@PathVariable Map<String, String> pathVarsMap) {

        byte[] fileContent = getBytes(pathVarsMap);
        String fileName = pathVarsMap.get("fileName");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/png; charset=UTF-8"));
        headers.add("content-disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

}
