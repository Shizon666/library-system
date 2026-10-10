package com.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.vo.BookSearchVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSerializationTest {

    @Test
    void bookSearchVoIsbnFieldName() throws Exception {
        BookSearchVO vo = new BookSearchVO();
        vo.setISBN("9787111680011");
        vo.setBookName("测试");

        String json = new ObjectMapper().writeValueAsString(vo);
        System.out.println("JSON: " + json);

        assertTrue(json.contains("\"ISBN\":\"9787111680011\""),
                "JSON should use uppercase ISBN key: " + json);
        assertNotNull(vo.getISBN());
    }
}
