import retrofit2.Call;
import retrofit2.http.GET;

public interface ChainService {

	@GET("api/chain/active")
	Call<Chain> getActiveChain();

}
