import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final String NASA_URL =
        "https://api.nasa.gov/planetary/apod?api_key="; // URL-адрес с запросом ключа
    public static final String USER_KEY =
        "dbZx0SsL05myfFXruLllRTJIbxBJvMhxfHgQkCNj"; // пользовательский ключ
    public static final String REMOTE_SERVICE_URI = toCreateRemoteServiceUri(); // URL-адрес с ключем
    public static ObjectMapper mapper = new ObjectMapper(); // создаем 'json mapper
    public static String log = ""; // создали лог для записи истории о создании файлов и директорий

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
            .setSocketTimeout(30000)    // максимальное время ожидания получения данных
            .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
            .build())
        .build();

        HttpGet requestOne = new HttpGet(REMOTE_SERVICE_URI); // содаем 1-ый объект запроса
        requestOne.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(requestOne); // отправляем 1-ый запрос

        Post post = mapper.readValue(response.getEntity().getContent(),
            new TypeReference<Post>() {}); // преобразовываем 'json' в 'java-объект'

        HttpGet requestTwo = new HttpGet(post.getUrl()); // содаем 2-ой объект запроса
        requestTwo.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        response = httpClient.execute(requestTwo); // отправляем 2-ой запрос

        String body = new String(response.getEntity().getContent()
            .readAllBytes(), StandardCharsets.UTF_8); // читаем тело ответа

        String nameOfFile = namingToFile(post.getUrl()); // получаем имя файла NASA

        // создаем директории для сохранения файла NASA и файла с логами
        String parentFolderOne = "D://NASA"; // путь к папке, где хотим создать директории
        List<String> directoriesOne = Arrays.asList("images", "temp"); // список создаваемых директорий
        for (String dir : directoriesOne) { // cоздаем дирректории согласно списка
            createDir(parentFolderOne, dir);
        }

        // создаем файл для записи данных с сайта NASA
        String parentFolderTwo = "D://NASA//images"; // указываем путь, где находится каталог
        createFile(parentFolderTwo, nameOfFile); // создаем файл

        // создаем файл для записи логов
        String parentFolderThree = "D://NASA//temp"; // указываем путь, где находится каталог
        createFile(parentFolderThree, "temp.txt"); // создаем файл

        // записываем информацию в файл на основе данных с сайта NASA
        recorderToFile(parentFolderTwo, nameOfFile, body);


        recorderLog("D://NASA//temp//temp.txt"); // записываем информацию из лога ф файл
    }

    // метод добавления пользовательского ключа к URL-адресу
    public static String toCreateRemoteServiceUri() {
        String RemoteServiceUri;
        StringBuilder value = new StringBuilder();
        value.append(NASA_URL);
        value.append(USER_KEY);
        RemoteServiceUri = value.toString();
        return  RemoteServiceUri;
    }

    // метод получения имени файла из URL-адреса
    public static String namingToFile(String url) {
        String[] partsOfUrl = url.split("/");
        String nameOfFile = partsOfUrl[6];
        return nameOfFile;
    }

    // метод для создания директорий
    public static void createDir(String parentFolder, String dir) {
        String newLog = null;
        StringBuilder value = new StringBuilder();
        value.append(parentFolder);
        value.append("//");
        value.append(dir);
        String preNewDir = value.toString();
        File newDir = new File(preNewDir);
        if (newDir.mkdir()) {
            newLog = "Создана директория: '" + preNewDir + "'";
        } else {
            newLog = "Ошибка создания директории: '" + preNewDir + "'";
        }
        System.out.println(newLog);
        addLog(newLog);
    }

    // метод для создания файлов
    public static void createFile(String dir, String file) {
        String newLog = null;
        StringBuilder value = new StringBuilder();
        value.append(dir);
        value.append("//");
        value.append(file);
        String preNewFile = value.toString();
        File newFile = new File(preNewFile);
        try {
            if (newFile.createNewFile())
                newLog = "Создан файл: '" + preNewFile + "'";
        } catch (IOException ex){
            newLog = ex.getMessage();
        }
        System.out.println(newLog);
        addLog(newLog);
    }

    // метод записи информации в файл
    public static void recorderToFile(String dir, String file, String body) {
        String newLog = null;
        StringBuilder value = new StringBuilder();
        value.append(dir);
        value.append("//");
        value.append(file);
        String recordingFile = value.toString();
        try (FileOutputStream fos = new FileOutputStream(recordingFile)) {
            byte[] bytes = body.getBytes();
            fos.write(bytes, 0, bytes.length);
            newLog = "Информация записана в файл: '" + recordingFile + "'";
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(newLog);
        addLog(newLog);
    }

    // метод для добавления записей в лог
    public static void addLog(String newLog) {
        StringBuilder value = new StringBuilder();
        value.append(log);
        value.append(newLog);
        value.append("\n");
        log = value.toString();
    }

    // метод для записи лога в файл temp.txt
    public static void recorderLog(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(log);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
