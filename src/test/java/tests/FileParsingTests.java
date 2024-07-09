package tests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import models.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileParsingTests {
    private final ClassLoader cl = FileParsingTests.class.getClassLoader();

    @DisplayName("Проверка данных из файлов pdf, xlsx, csv. Файлы извлечены из zip архива")
    @Test
    void zipFileParsingTest() throws Exception {
        List<String> expectedPdfValues = List.of("sample", "Philip Hutchison");
        List<String> expectedXlsxValues = List.of("Азбуков В.", "Арбузова А.", "Барабанов С.");
        List<String[]> expectedScvValues = new ArrayList<>();
        expectedScvValues.add(new String[]{"eruid", "description"});
        expectedScvValues.add(new String[]{"batman", "uses technology"});

        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("forParsingFiles.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case "forParsingFiles/sample.pdf" -> checkPdf(zis, expectedPdfValues);
                    case "forParsingFiles/exampleExcel.xlsx" -> checkXlsx(zis, expectedXlsxValues);
                    case "forParsingFiles/exampleCsv.csv" -> checkCsv(zis, expectedScvValues);
                }
            }
        }
    }

    @DisplayName("Проверка данных json файла")
    @Test
    void jsonCheckTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("student.json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            Student student = objectMapper.readValue(is, Student.class);
            assertEquals("Victor", student.getName());
            assertEquals(21245, student.getId());
            assertEquals("Minsk", student.getAddress().getCity());
            assertEquals("Pushkina", student.getAddress().getStreet());
            assertEquals(20, student.getAddress().getHouseNumber());
            assertEquals(List.of("Math", "Physics", "Statistics"), student.getSubjects());
        }
    }

    private void checkPdf(ZipInputStream zis, List<String> expectedValues) throws IOException {
        PDF pdf = new PDF(zis);
        assertEquals(expectedValues.getFirst(), pdf.title);
        assertEquals(expectedValues.get(1), pdf.author);
    }

    private void checkXlsx(ZipInputStream zis, List<String> expectedValues) throws IOException {
        XLS xlsFile = new XLS(zis);
        String firstName = xlsFile.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
        String secondName = xlsFile.excel.getSheetAt(0).getRow(1).getCell(0).getStringCellValue();
        String thirdName = xlsFile.excel.getSheetAt(0).getRow(2).getCell(0).getStringCellValue();
        assertEquals(expectedValues.getFirst(), firstName);
        assertEquals(expectedValues.get(1), secondName);
        assertEquals(expectedValues.getLast(), thirdName);
    }

    private void checkCsv(ZipInputStream zis, List<String[]> expectedValues) throws Exception {
        CSVReader csvFile = new CSVReader(new InputStreamReader(zis));
        List<String[]> data = csvFile.readAll();
        assertEquals(Arrays.toString(expectedValues.getFirst()), Arrays.toString(data.get(0)));
        assertEquals(Arrays.toString(expectedValues.get(1)), Arrays.toString(data.get(1)));
    }
}
