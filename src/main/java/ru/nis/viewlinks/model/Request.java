package ru.nis.viewlinks.model;

import lombok.Data;

@Data
public class Request {
    String id;
    String firstName;
    String lastName;
    String patronymic;
    String fileName;
    String extensionFile;
}
