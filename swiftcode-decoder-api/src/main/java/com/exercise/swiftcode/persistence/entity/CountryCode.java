package com.exercise.swiftcode.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "country_codes")
@Data
@AllArgsConstructor
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CountryCode {
    @Id
    String id;

    @Field("COUNTRYNAME")
    String countryName;

    @Field("COUNTRYISO2CODE")
    String countryIso2Code;
}