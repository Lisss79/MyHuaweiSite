package com.example.myhuaweisite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.HWLocation;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.HwLocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String MAX_DISTANCE_KEY = "MAX_DISTANCE";
    private static final String MAX_DISTANCE_RANGE_KEY = "MAX_DISTANCE_RANGE";
    private static final String SELECTED_OBJECTS_KEY = "SELECTED_OBJECTS";
    public static final String LATITUDE_KEY = "LATITUDE";
    public static final String LONGITUDE_KEY = "LONGITUDE";
    public static final String ADDRESS_KEY = "ADDRESS";
    public static final String NAME_KEY = "NAME";
    public static final String API_KEY = "API";
    public static final int NO_ERROR = 0;
    public static final int NO_INTERNET = 1;
    public static final int INTERNAL_ERROR = 2;
    public static final int NO_DATA = 3;
    private static final int ERROR = 10;

    private FusedLocationProviderClient locationClient;
    private SearchService searchService;
    private TextView textViewWarning;
    private SeekBar seekBarDistance;
    private ProgressBar progressBarSearch;
    private ListView listViewResult;
    private Spinner spinnerObject;
    private EditText editTextDistance;
    private List<HashMap<String, String>> arrayList;
    private SimpleAdapter adapter;
    private Locale locale;
    private int maxDistance = 1000;
    private int maxDistanceRange = 2000;
    private InputMethodManager inputMethodManager;
    private Handler locationHandler;
    private Handler addressHandler;
    private Handler searchHandler;

    private String[] distanceArray = {"100m", "500m", "1km", "2km", "5km", "10km", "20km"};
    private int[] distanceMetersArray = {100, 500, 1000, 2000, 5000, 10000, 20000};
    private final String[] objects = {"Супермаркеты", "Общепит", "Банкоматы", "Заправки", "Банки",
            "Аптеки", "Кинотеатры", "Музеи", "Театры"};
    private final HwLocationType[] objectTypes = {HwLocationType.SUPERMARKET,
            HwLocationType.EATING_DRINKING, HwLocationType.ATM, HwLocationType.PETROL_STATION,
            HwLocationType.BANK, HwLocationType.PHARMACY, HwLocationType.CINEMA, HwLocationType.MUSEUM,
            HwLocationType.THEATER};
    private final boolean[] objectsSel = {true, false, false, false, false, false, false, false, false};
    private ArrayList<HwLocationType> selectedObjects = new ArrayList<>();
    private HashMap<String, String> map = new HashMap<>();
    private ArrayList<Site> allSites = new ArrayList<>();

    private SharedPreferences preferences;
    private String sharedPrefFile = "com.example.myhuaweisite";

    private String error; // служебная переменная
    private int errorCode = NO_ERROR; // служебная переменная
    private int tempCounter = 0; // служебная переменная

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean isHMS = checkHMSStatus();
        checkForInternetAccess();

        listViewResult = findViewById(R.id.listViewResult);
        arrayList = new ArrayList<>();
        adapter = new SimpleAdapter(MainActivity.this, arrayList,
                android.R.layout.simple_list_item_2, new String[]{"Name", "Address"},
                new int[]{android.R.id.text1, android.R.id.text2});
        listViewResult.setAdapter(adapter);
        listViewResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Site site = allSites.get(position);
                HashMap<String, String> currMap = arrayList.get(position);
                //double lat = site.getLocation().getLat();
                //double lon = site.getLocation().getLng();
                //long dist = Math.round(site.getDistance());
                //String name = site.getName();
                double lat = Double.valueOf(currMap.get("Lat"));
                double lon = Double.valueOf(currMap.get("Lon"));
                String name = currMap.get("Name");
                String addr = currMap.get("Address");

                Intent intent = new Intent(MainActivity.this, ObjectDetailActivity.class);
                intent.putExtra(LATITUDE_KEY, lat);
                intent.putExtra(LONGITUDE_KEY, lon);
                intent.putExtra(NAME_KEY, name);
                intent.putExtra(ADDRESS_KEY, addr);

                intent.putExtra(API_KEY, getApiKey());
                startActivity(intent);
            }
        });
        locale = new Locale("ru", "RU");

        textViewWarning = findViewById(R.id.textViewWarning);
        searchService = SearchServiceFactory.create(this, getApiKey());
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        progressBarSearch = findViewById(R.id.progressBarSearch);
        progressBarSearch.setVisibility(View.GONE);

        restoreData();

        seekBarDistance = findViewById(R.id.seekBarDistance);
        seekBarDistance.setMax(maxDistanceRange);
        seekBarDistance.setProgress(maxDistance);
        editTextDistance.setText(String.valueOf(maxDistance));
        editTextDistance.setBackgroundColor(Color.TRANSPARENT);
        editTextDistance.setOnClickListener(view -> editTextDistance.setCursorVisible(true));
        editTextDistance.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        doneTyping();
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });

        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                maxDistance = i;
                editTextDistance.setText(String.valueOf(maxDistance));
                saveData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button buttonGetData = findViewById(R.id.buttonGetData);
        if (!isHMS) {
            buttonGetData.setEnabled(false);
            textViewWarning.setText("Install or repair HMS first");
            textViewWarning.setVisibility(View.VISIBLE);
        } else {
            buttonGetData.setOnClickListener(v -> {
                if (inputMethodManager.isAcceptingText()) {
                    doneTyping();
                }
                arrayList.clear();
                adapter.notifyDataSetChanged();
                progressBarSearch.setVisibility(View.VISIBLE);
                locationHandler = new Handler(getMainLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == NO_ERROR) {
                            double latitude = msg.getData().getDouble(LATITUDE_KEY, 0);
                            double longitude = msg.getData().getDouble(LONGITUDE_KEY, 0);
                            searchHandler = new Handler(getMainLooper()) {
                                @Override
                                public void handleMessage(@NonNull Message msg) {
                                    super.handleMessage(msg);
                                    if(msg.what == NO_ERROR) {
                                        progressBarSearch.setVisibility(View.GONE);
                                    }
                                }
                            };
                            searchData(latitude, longitude, getApiKey());
                        } else {
                            progressBarSearch.setVisibility(View.GONE);
                            showErrorMessage("No location data");
                        }
                    }
                };
                getLocation();
            });
        }

        Button buttonWhereAmI = findViewById(R.id.buttonWhereAmI);
        if (isHMS) {
            buttonWhereAmI.setOnClickListener(v -> {
                if (inputMethodManager.isAcceptingText()) {
                    doneTyping();
                }
                progressBarSearch.setVisibility(View.VISIBLE);
                locationHandler = new Handler(getMainLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == NO_ERROR) {
                            double latitude = msg.getData().getDouble(LATITUDE_KEY, 0);
                            double longitude = msg.getData().getDouble(LONGITUDE_KEY, 0);

                            addressHandler = new Handler(getMainLooper()) {
                                @Override
                                public void handleMessage(@NonNull Message msg) {
                                    super.handleMessage(msg);
                                    if (msg.what == NO_ERROR) {
                                        progressBarSearch.setVisibility(View.GONE);
                                        String address = msg.getData().getString(ADDRESS_KEY, "No address :-(");
                                        showWhereAmIActivity(latitude, longitude, address);

                                    } else {
                                        progressBarSearch.setVisibility(View.GONE);
                                        showWhereAmIActivity(latitude, longitude, "No address :-(");
                                    }
                                }
                            };
                            getAddress(latitude, longitude);

                        } else {
                            progressBarSearch.setVisibility(View.GONE);
                            showErrorMessage("No location data");
                        }
                    }
                };
                getLocation();

            });
        } else {
            buttonWhereAmI.setEnabled(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void showWhereAmIActivity(double latitude, double longitude, String address) {

        Intent intent = new Intent(MainActivity.this, WhereAmIActivity.class);
        intent.putExtra(LATITUDE_KEY, latitude);
        intent.putExtra(LONGITUDE_KEY, longitude);
        intent.putExtra(ADDRESS_KEY, address);
        intent.putExtra(API_KEY, getApiKey());
        startActivity(intent);

    }

    private void showDialogSelectObjects() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select objects:"); // заголовок окна
        builder.setIcon(android.R.drawable.ic_menu_edit); // иконка в заголовке
        builder.setMultiChoiceItems(objects, objectsSel, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                objectsSel[i] = b;
                saveData();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void checkForInternetAccess() {
        Handler handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                TextView textView = findViewById(R.id.textViewInternetStatus);
                String text = msg.what == NO_ERROR ? "OK" : "no connecton";
                int color = msg.what == NO_ERROR ? Color.GREEN : Color.RED;
                textView.setTextColor(color);
                textView.setText(text);
            }
        };

        Runnable runnable = () -> {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");
                handler.sendEmptyMessage(NO_ERROR);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(ERROR);
            }
        };
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        service.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_max_distance:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select max distance"); // заголовок окна
                builder.setIcon(android.R.drawable.ic_menu_edit); // иконка в заголовке
                // строковый массив
                builder.setItems(distanceArray, (dialog, which) -> {
                    maxDistanceRange = distanceMetersArray[which];
                    seekBarDistance.setMax(maxDistanceRange);
                    if (maxDistance > maxDistanceRange) {
                        maxDistance = maxDistanceRange;
                        seekBarDistance.setProgress(maxDistance);
                    }
                    saveData();
                });
                builder.show();
                return true;
            case R.id.menu_set_objects:
                showDialogSelectObjects();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    /**
     * Загрузка данных из SharedPreferences
     */
    private void restoreData() {
        editTextDistance = findViewById(R.id.editTextDistance);
        maxDistance = preferences.getInt(MAX_DISTANCE_KEY, maxDistance);
        maxDistanceRange = preferences.getInt(MAX_DISTANCE_RANGE_KEY, maxDistanceRange);
        objectsSel[0] = preferences.getBoolean(SELECTED_OBJECTS_KEY.concat("0"), true);
        for (int i = 1; i < objectsSel.length; i++) {
            objectsSel[i] = preferences.getBoolean(SELECTED_OBJECTS_KEY.concat(String.valueOf(i)), false);
        }
    }

    /**
     * Сохранение данных в SharedPreferences
     */
    private void saveData() {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt(MAX_DISTANCE_KEY, maxDistance);
        preferencesEditor.putInt(MAX_DISTANCE_RANGE_KEY, maxDistanceRange);
        for (int i = 0; i < objectsSel.length; i++) {
            preferencesEditor.putBoolean(SELECTED_OBJECTS_KEY.concat(String.valueOf(i)), objectsSel[i]);
        }
        preferencesEditor.apply();
    }

    /**
     * Завершение ввода расстояния и передача значения
     */
    private void doneTyping() {
        String newValue = editTextDistance.getText().toString();
        if (newValue.length() == 0) newValue = "1";
        maxDistance = Integer.parseInt(newValue);
        seekBarDistance.setProgress(maxDistance);
        editTextDistance.setCursorVisible(false);
        inputMethodManager.hideSoftInputFromWindow(editTextDistance.getWindowToken(), 0);
    }

    /**
     * Запрос прав доступа на чтение геопозиции
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // If the permission is granted, get the location,
            // otherwise, show a Toast
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) getLocation();
            else {
                // Обработать!
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Проверка статуса HMS
     *
     * @return true - если HMS доступны, иначе false
     */
    private boolean checkHMSStatus() {
        boolean result = false;
        String text;
        int color = Color.YELLOW;
        HuaweiApiAvailability huaweiApiAvailability = HuaweiApiAvailability.getInstance();
        int status = huaweiApiAvailability.isHuaweiMobileNoticeAvailable(this);
        switch (status) {
            case 0:
                text = "OK";
                result = true;
                color = Color.GREEN;
                break;
            case 1:
                text = "not installed";
                color = Color.RED;
                break;
            case 2:
                text = "out of date";
                break;
            default:
                text = "unknown error";
                break;
        }
        TextView textView = findViewById(R.id.textViewHMSStatus);
        textView.setText(text);
        textView.setTextColor(color);
        return result;
    }

    /**
     * Получение текущих координат
     */
    private void getLocation() {
        textViewWarning.setVisibility(View.GONE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    //HWLocation hwLocation = locationResult.getLastHWLocation();
                    if (location != null) {
                        // Код при получении значения локации
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putDouble(LATITUDE_KEY, latitude);
                        data.putDouble(LONGITUDE_KEY, longitude);
                        msg.what = NO_ERROR;
                        msg.setData(data);
                        locationHandler.sendMessage(msg);
                        locationClient.removeLocationUpdates(this);
                    } else {
                        // Код при отсутствии значения
                        Log.i("Location ", "no data");
                        Message msg = new Message();
                        msg.what = ERROR;
                        locationHandler.sendMessage(msg);
                        locationClient.removeLocationUpdates(this);
                    }
                }
            };
            locationClient = LocationServices.getFusedLocationProviderClient(this);


            locationClient.getLastLocationWithAddress(getLocationRequest()).addOnSuccessListener(new OnSuccessListener<HWLocation>() {
                @Override
                public void onSuccess(HWLocation hwLocation) {
                    System.out.println(hwLocation);
                }
            });

            locationClient.requestLocationUpdates(getLocationRequest(),
                    locationCallback, getMainLooper());

        }
    }

    /**
     * Получение LocationRequest
     *
     * @return
     */
    private LocationRequest getLocationRequest() {
        // Частота запросов – раз в 5000мс, приоритет – высокий
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    /**
     * Получение адреса по координатам
     */
    private void getAddress(double latitude, double longitude) {
        //latitude = 55.914876563430454;
        //longitude = 36.79219668459067;

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            // If an address is found, read it into resultMessage
            Address address = addresses.get(0);

            String addr = getMyAddressText(address);
            Message msg = new Message();
            msg.what = NO_ERROR;
            Bundle data = new Bundle();
            data.putString(ADDRESS_KEY, addr);
            msg.setData(data);
            addressHandler.sendMessage(msg);

        } catch (IOException e) {
            // Ошибка – адрес не получен
            e.printStackTrace();
            Message msg = new Message();
            msg.what = ERROR;
            addressHandler.sendMessage(msg);
        }
    }

    /**
     * Получение подробного моего адреса для отображения
     * @param address
     * @return
     */
    private String getMyAddressText(Address address) {
        StringBuilder builder = new StringBuilder();
        builder.append(stringToAdd(address.getThoroughfare(), false, false));
        builder.append(stringToAdd(address.getSubThoroughfare(), false, true));
        if(!address.getLocality().equals(address.getSubLocality()))
            builder.append(stringToAdd(address.getSubLocality(), false, false));
        builder.append(stringToAdd(address.getLocality(), false, true));
        if (!address.getLocality().equals(address.getSubAdminArea()))
            builder.append(stringToAdd(address.getSubAdminArea(), false, false));
        if (!address.getLocality().equals(address.getAdminArea()))
            builder.append(stringToAdd(address.getAdminArea(), false, true));
        builder.append(stringToAdd(address.getCountryName(), false, false));
        builder.append(stringToAdd(address.getPostalCode(), true, false));
        return builder.toString();
    }

    /**
     * Возвращает строку, которубю нужно добавить к адресу
     *
     * @param s
     * @param end
     */
    private String stringToAdd(String s, boolean end, boolean nextLine) {
        String res = "";
        if (s != null) res += s;
        if (!end && s != null) res += ", ";
        if (s != null && nextLine) res += "\n";
        return res;
    }

    /**
     * Создание списка, содержащего выбранные типы объектов
     */
    private void createSelectedObjects() {
        selectedObjects.clear();
        int i = 0;
        while (i < objectTypes.length) {
            if (objectsSel[i]) selectedObjects.add(objectTypes[i]);
            i++;
        }
    }

    /**
     * Поиск объектов около местоположения
     *
     * @param latitude  широта места поиска
     * @param longitude долгота места поиска
     * @param apiKey    ключ API HMS
     */
    private void searchData(double latitude, double longitude, String apiKey) {
        //searchService = SearchServiceFactory.create(this, apiKey);
        createSelectedObjects();
        allSites.clear();

        // Create a request body.
        NearbySearchRequest request = new NearbySearchRequest();
        Coordinate location = new Coordinate(latitude, longitude);
        request.setLocation(location);
        request.setRadius(maxDistance);
        request.setPageIndex(1);
        request.setPageSize(20);

        //Заполняем список ListView
        //ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        arrayList.clear();

        SearchResultListener<NearbySearchResponse> resultListener = new SearchResultListener<NearbySearchResponse>() {

            // Return search results upon a successful search.
            @Override
            public void onSearchResult(NearbySearchResponse results) {
                tempCounter++;

                if (errorCode == NO_ERROR) {
                    if (results == null || results.getTotalCount() <= 0) {
                        sortAndShowResultsIfEnd();
                        return;
                    }
                    List<Site> sites = results.getSites();
                    if (sites == null || sites.size() == 0) {
                        sortAndShowResultsIfEnd();
                        return;
                    }

                    for (Site site : sites) {
                        map = new HashMap<>();
                        map.put("Name", site.getName());
                        long distance = Math.round(site.getDistance());

                        String address = getAddressText(site, (int) distance);

                        map.put("Address", address);
                        map.put("Distance", site.getDistance().toString());
                        map.put("Lat", String.valueOf(site.getLocation().getLat()));
                        map.put("Lon", String.valueOf(site.getLocation().getLng()));
                        arrayList.add(map);
                    }
                    allSites.addAll(sites);
                }
                sortAndShowResultsIfEnd();
            }

            // Return the result code and description upon a search exception.
            @Override
            public void onSearchError(SearchStatus status) {
                tempCounter++;
                if (errorCode == NO_ERROR) {
                    switch (status.getErrorCode()) {
                        case "010004":
                            break;
                        case "070003":
                        case "070005":
                            errorCode = NO_INTERNET;
                            break;
                        default:
                            errorCode = INTERNAL_ERROR;
                            error = status.getErrorCode();
                            break;
                    }
                }
                //showErrorMessage(text);
                sortAndShowResultsIfEnd();
            }
        };

        tempCounter = 0;
        errorCode = NO_ERROR;
        for (HwLocationType type : selectedObjects) {
            request.setHwPoiType(type);
            // Call the nearby place search API.
            searchService.nearbySearch(request, resultListener);

        }

    }

    /**
     * Получние адреса для отображения
     * @param site
     * @return
     */
    private String getAddressText(Site site, int distance) {
        String address = String.format(locale, "%s, %s, %s; %dm",
                site.getAddress().getLocality(), site.getAddress().getThoroughfare(),
                site.getAddress().getStreetNumber(), distance);
        if (site.getAddress().getLocality() == null) {
            address = String.format(locale, "Address is unknown; %dm", distance);
        }
        return address;
    }

    /**
     * Проверить, выполнены ли все запросы и если да,
     * то отсортировать и показать данные или ошибку
     */
    private void sortAndShowResultsIfEnd() {
        if (tempCounter >= selectedObjects.size()) {
            if (arrayList.isEmpty() && errorCode == NO_ERROR) errorCode = NO_DATA;
            switch (errorCode) {
                case NO_INTERNET:
                    showErrorMessage("Internet connection problem");
                    break;
                case INTERNAL_ERROR:
                    String text = "Internal error. Can't receive data.\n" + error;
                    showErrorMessage(text);
                    break;
                case NO_DATA:
                    showErrorMessage("No data found :(");
                    break;
                default: // нет ошибок
                    arrayList.sort((m1, m2) -> {
                        double distance1 = Double.parseDouble(m1.get("Distance"));
                        double distance2 = Double.parseDouble(m2.get("Distance"));
                        return distance1 > distance2 ? 1 : -1;
                    });
                    adapter.notifyDataSetChanged();
                    break;
            }
            searchHandler.sendEmptyMessage(NO_ERROR);
        }
    }

    private void showErrorMessage(String text) {
        textViewWarning.setText(text);
        textViewWarning.setVisibility(View.VISIBLE);
        arrayList.clear();
        adapter.notifyDataSetChanged();
    }

    private String getApiKey() {
        String key = "";
        try {
            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            key = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String encodedApiKey = "";
        try {
            encodedApiKey = URLEncoder.encode(key, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(getPackageName(), "encode apikey error");
        }

        return encodedApiKey;
    }

}