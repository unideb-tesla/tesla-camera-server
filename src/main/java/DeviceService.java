import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface DeviceService {

	@GET("api/device")
	Call<List<Device>> getAll();

}
