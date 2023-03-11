package yt.graven.gravensupport.utils.messages.serializable;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializableMessageAuthor {
  @Expose public long id = 0L;
  @Expose public String name = "";
  @Expose public String avatarUrl = "";
}
