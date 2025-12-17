import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestHTTPConnection {
    public static void main(String[] args) throws IOException {
        String token = "69e5f109-c4b1-402c-9137-bdf571ce3b1c";
        String url = "https://api.weather.yandex.ru/v2/forecast?lat=52.37125&lon=40.89388&lang=ru_RU&limit=1&hours=false&extra=true";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Yandex-Weather-Key", token);
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        parseAndPrintWeather(response.toString());
    }

    private static void parseAndPrintWeather(String jsonResponse) {
        try {
            String location = extractValue(jsonResponse, "\"name\":\"", "\"");
            String temp = extractValue(jsonResponse, "\"temp\":", ",");
            String feelsLike = extractValue(jsonResponse, "\"feels_like\":", ",");
            String condition = extractValue(jsonResponse, "\"condition\":\"", "\"");
            String windSpeed = extractValue(jsonResponse, "\"wind_speed\":", ",");
            String windDir = extractValue(jsonResponse, "\"wind_dir\":\"", "\"");
            String humidity = extractValue(jsonResponse, "\"humidity\":", ",");
            String precProb = extractValue(jsonResponse, "\"prec_prob\":", ",");
            String pressure = extractValue(jsonResponse, "\"pressure_mm\":", ",");
            String daytime = extractValue(jsonResponse, "\"daytime\":\"", "\"");
            String season = extractValue(jsonResponse, "\"season\":\"", "\"");

            String time = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

            System.out.println("\n" + "=".repeat(48));
            System.out.println("              Погода сейчас:");
            System.out.println("=".repeat(48));

            System.out.println("Место:       " + location);
            System.out.println("Время запроса: " + time);
            System.out.println("-".repeat(48));

            System.out.println("Температура: " + temp + "°C");
            System.out.println("Чувствуется как: " + feelsLike + "°C");
            System.out.println("Состояние:   " + condition);
            System.out.println("Ветер:       "  + windDir);
            System.out.println("Влажность:   " + humidity + "%");
            System.out.println("Вероятность осадков: " + precProb + "%");
            System.out.println("Давление:    " + pressure + " мм рт.ст.");
            System.out.println("Время суток: " + (daytime.equals("d") ? "день" : "ночь"));
            System.out.println("Сезон:       " + season);

            System.out.println("=".repeat(48));

        } catch (Exception e) {
            System.out.println("Ошибка при обработке данных: " + e.getMessage());
            System.out.println("\nСырой ответ:\n" + jsonResponse);
        }
    }

    private static String extractValue(String json, String startMarker, String endMarker) {
        int startIndex = json.indexOf(startMarker);
        if (startIndex == -1) return "N/A";

        startIndex += startMarker.length();
        int endIndex = json.indexOf(endMarker, startIndex);
        if (endIndex == -1) endIndex = json.length();

        return json.substring(startIndex, endIndex);
    }
}
// протоколы передачи данных
//abstract class Protocol{
//    public String ipSender;
//    public String ipRecipent;
//    abstract public void send(String message) throws FileNotFoundException;
//    abstract public void receive();
//}
//
//class TCPProtocol extends Protocol{
//    public TCPProtocol(String ipSender, String ipRecipent){
//        this.ipSender = ipSender;
//        this.ipRecipent = ipRecipent;
//    }
//
//    public void send(String message) throws FileNotFoundException {
//        PrintWriter out = new PrintWriter(this.ipSender);
//        out.println(message);
//        out.close();
//    }
//
//    public void receive() throws FileNotFoundException {
//        Scanner sc = new Scanner(FileInputStream(this.ip))
//        String message = sc.nextline();
//    }
//
//}
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.util.*;
//
//class Main{
//    public static void main(String[] args) throws FileNotFoundException {
//        //System.out.println("Hello world");
//        int a = 10;
//        int b = 20;
//        System.out.println(a+b);
//        char ch = 'a';
//        System.out.println((char)(ch-1));
//        boolean bl = 10*12<100;
//        if(bl && true || false){
//            System.out.println("true");
//        }else {
//            System.out.println("false");
//        }
//        System.out.println(123. / 10);
//        System.out.println(123 % 10);
//        System.out.println(12==12);
//        System.out.println(!(12==12));
//        a = 0;
//        while(a<10)
//        {
//            System.out.println(a);
//            a++;
//        }
//        for(int i = 0; i < 19; i++) {
//            System.out.print(i);
//        }
//        System.out.println();
//        for (int i = 1; i < 11; i++) {
//            System.out.println((int)Math.pow(i, 2));
//        }
//        for (int i = 2; i < 11; i+=2) {
//            System.out.println((int)Math.pow(i, 2));
//        }
//        int fib = 1;
//        int prev = 1;
//        System.out.println(fib);
//        while(fib+prev<100)
//        {
//            int c = fib+=prev;
//            System.out.println(c);
//            prev = fib;
//            fib+=c;
//        }
//        System.out.println(even(10));
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 5; j++) {
//                if(even(i+j))
//                {
//                    System.out.print('_');
//                }
//                else System.out.print('*');
//            }
//            System.out.println();
//        }
//        Scanner sc = new Scanner(new FileInputStream("input.txt"));
//        String name = sc.next();
//        PrintWriter pw = new PrintWriter("output.txt");
//        System.out.printf("Your name is: "+ name);
//        pw.println("your name is:"+name);
//        pw.close();
//        Scanner sc = new Scanner(System.in);
//        int n = sc.nextInt();
//        String s = "_";
//        for (int i = 1; i < n; i++) {
//            System.out.print(i);
//            System.out.print(s);
//            s+= "_";
//        }
//      Scanner sc = new Scanner(System.in);
//        int n = sc.nextInt();
//        int[][] arr = new int[n][n];
//        arr[0][1] = 1;
//        arr[0][0] = 1;
//        arr[1][0] = 1;
//        for (int i = 2; i < n; i++) {
//            arr[0][i] = arr[0][i-1]+arr[0][i-2];
//            arr[i][0] = arr[i-1][0]+arr[i-2][0];
//        }
//        for (int i = 1; i < n; i++) {
//            for (int j = 1; j < n; j++) {
//                arr[i][j] = arr[i][j-1]+arr[i-1][j-1];
//            }
//        }
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                System.out.print(arr[i][j]+" ");
//            }
//            System.out.println();
//        }
        //а можно все поссчитать сразу
//        ArrayList<Integer> list = new ArrayList<>();
//        Scanner sc = new Scanner(System.in);
//        int n = sc.nextInt();
//        int ar[][] = new int[n][n];
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                ar[i][j] = 0;
//            }
//        }
//        ar[1][2] = 1;
//        ar[2][1] = 1; ar[2][2] = 1; ar[2][3] = 1; ar[2][4] = 1;
//        ar[3][2] = 1; ar[3][4] = 1; ar[3][5] = 1; ar[3][6] = 1;
//        ar[4][2] = 1; ar[4][3] = 1;
//        ar[5][3] = 1;
//        ar[6][3] = 1;
//
//        ArrayList<Integer>[] list = new ArrayList[n];
//        int max = 0;
//        int maxin = -1;
//        for (int i = 0; i < n; i++) {
//            list[i] = new ArrayList<>();
//            for (int j = 0; j < n; j++) {
//                if(ar[i][j] == 1)
//                {
//                    list[i].add(j+1);
//                }
//                if (max < list[i].size())
//                {
//                    max = list[i].size();
//                    maxin = i+1;
//                }
//            }
//        }
//        HashSet<Integer> set = new HashSet<>();
//        set.add(0);
//        set.add(0);
//        System.out.println(set.size());
//        System.out.println(set.contains(2));
//        Scanner sc = new Scanner(System.in);
//        int n = sc.nextInt();
//        String name;
//        int val;
//        HashMap<String, Integer> map = new HashMap<>();
//        for (int i = 0; i < n; i++) {
//            name = sc.next();
//            val = sc.nextInt();
//            if(map.containsKey(name))
//            {
//                val += map.get(name);
//                map.remove(name);
//                map.put(name, val);
//            }
//            else {
//                map.put(name, val);
//            }
//        }
//    }
//    public static boolean even(int a)
//    {
//        if(a%2 == 0) return true;
//        else return false;
//    }
//}
// int 4 байта
// short 2 байт
// long 8 байт
// double 8 байт
// char 8 байта
// float 4 байта
// byte 1 байт
// boolean 1 байт

