package com.github.mfnsvrtm;

import com.github.rvesse.airline.parser.errors.ParseOptionConversionException;
import com.github.rvesse.airline.types.DefaultTypeConverter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class ExtendedTypeConverter extends DefaultTypeConverter {
    @Override
    public Object convert(String name, Class<?> type, String value) {
        checkArguments(name, type, value);

        if (Path.class.isAssignableFrom(type)) {
            try {
                return Path.of(value);
            } catch (InvalidPathException e) {
                throw new ParseOptionConversionException(name, value, type.getSimpleName());
            }
        } else if (type == LineEnding.class) {
            return LineEnding.valueOf(value.toUpperCase());
        }

        return super.convert(name, type, value);
    }
}
