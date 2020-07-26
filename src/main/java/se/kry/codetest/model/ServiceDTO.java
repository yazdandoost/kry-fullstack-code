package se.kry.codetest.model;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ServiceDTO {

  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");;

  static {
    TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("CET"));
  }

  private final Integer id;
  private final String name;
  private final String url;
  private final String status;
  private final String created;

  public static ServiceDTO from(Service service) {
    return new ServiceDTO(service.getId(), service.getName(), service.getUrl(),
        service.getStatus().name(), TIME_FORMAT.format(Date.from(service.getCreated())));
  }

  public ServiceDTO(Integer id, String name, String url, String status, String created) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.status = status;
    this.created = created;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getStatus() {
    return status;
  }

  public String getCreated() {
    return created;
  }
}
