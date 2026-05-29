package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CryptoPriceResponse
import com.example.model.YahooSparkItem
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MarketsUiState {
    object Loading : MarketsUiState()
    data class Success(
        val yahooData: Map<String, YahooSparkItem>,
        val cryptoData: Map<String, CryptoPriceResponse>,
        val timestamp: Long
    ) : MarketsUiState()
    data class Error(val message: String) : MarketsUiState()
}

class MarketsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MarketsUiState>(MarketsUiState.Loading)
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()

    private val cryptoIds = "bitcoin,ethereum,binancecoin,solana,ripple,dogecoin"
    private val yahooSymbols = "^BSESN,^NSEI,^NSEBANK,^NSMIDCP,^IXIC,^GSPC,^DJI,^FTSE,^N225,GC=F,SI=F,CL=F,BZ=F,NG=F,INR=X,EURINR=X,GBPINR=X,JPYINR=X"

    init {
        fetchMarketsData()
        
        // Auto-refresh every 60 seconds
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                fetchMarketsData(isSilent = true)
            }
        }
    }

    fun fetchMarketsData(isSilent: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isSilent && _uiState.value !is MarketsUiState.Success) {
                _uiState.value = MarketsUiState.Loading
            }

            try {
                val yahooDef = async { RetrofitClient.yahooApi.getSpark(symbols = yahooSymbols) }
                val cryptoDef = async { RetrofitClient.coinGeckoApi.getPrices(ids = cryptoIds) }
                
                val yahooRes = yahooDef.await()
                val cryptoRes = cryptoDef.await()

                _uiState.value = MarketsUiState.Success(
                    yahooData = yahooRes,
                    cryptoData = cryptoRes,
                    timestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                if (!isSilent) {
                    _uiState.value = MarketsUiState.Error(e.localizedMessage ?: "Failed to load market data")
                }
            }
        }
    }
}
