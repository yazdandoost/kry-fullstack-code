package se.kry.codetest.model;


import java.time.Instant;

public class Service {

  public enum Status {
    OK, FAIL, UNKNOWN;

    public static Status from(String s) {
      try {
        return Status.valueOf(s);
      } catch (Exception e) {
        return UNKNOWN;
      }
    }
  }

  public Service(Integer id, String name, String url, Status status, Instant created) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.status = status;
    this.created = created;
  }

  private final Integer id;
  private final String name;
  private final String url;
  private final Status status;
  private final Instant created;

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public Status getStatus() {
    return status;
  }

  public Instant getCreated() {
    return created;
  }
}
