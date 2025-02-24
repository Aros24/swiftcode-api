package com.exercise.swiftcode.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "banks")
@Data
@AllArgsConstructor
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Bank {
    @Id
    String id;

    @Field("COUNTRY ISO2 CODE")
    String countryIso2Code;

    @Field("SWIFT CODE")
    String swiftCode;

    @Field("NAME")
    String name;

    @Field("ADDRESS")
    String address;

    @Field("COUNTRY NAME")
    String countryName;
}