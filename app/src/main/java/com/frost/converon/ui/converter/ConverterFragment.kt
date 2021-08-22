package com.frost.converon.ui.converter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.frost.converon.R
import com.frost.converon.databinding.FragmentConverterBinding
import com.frost.converon.helper.EndPoints
import com.frost.converon.helper.Resource
import com.frost.converon.helper.Utility
import com.frost.converon.model.Rates
import com.google.android.material.snackbar.Snackbar
import java.util.*

class ConverterFragment : Fragment() {

    private lateinit var viewModel: ConverterViewModel
    private var _binding: FragmentConverterBinding?= null
    //Selected country string, default is Afghanistan, since its the first country listed in the spinner
    private var selectedItem1: String? = "AFN"
    private var selectedItem2: String? = "AFN"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(context as FragmentActivity).get(ConverterViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentConverterBinding.inflate(inflater, container, false)
        Utility.makeStatusBarTransparent(context as Activity)
        initSpinner()
        setUpClickListener()
        subscribeToLiveData()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * This method does everything required for handling spinner (Dropdown list) - showing list of countries, handling click events on items selected.*
     */

    private fun initSpinner(){

        val spinner1 = binding.spnFirstCountry

        spinner1.setItems( getAllCountries() )

        spinner1.setOnClickListener {
            Utility.hideKeyboard(context as Activity)
        }

        spinner1.setOnItemSelectedListener { _, _, _, item ->
            val countryCode = getCountryCode(item.toString())
            val currencySymbol = getSymbol(countryCode)
            selectedItem1 = currencySymbol
            binding.txtFirstCurrencyName.text = selectedItem1
        }


        val spinner2 = binding.spnSecondCountry

        spinner1.setOnClickListener {
            Utility.hideKeyboard(context as Activity)
        }
        spinner2.setItems( getAllCountries() )

        spinner2.setOnItemSelectedListener { _, _, _, item ->
            //Set the currency code for each country as hint
            val countryCode = getCountryCode(item.toString())
            val currencySymbol = getSymbol(countryCode)
            selectedItem2 = currencySymbol
            binding.txtSecondCurrencyName.text = selectedItem2
        }

    }


    /**
     * A method for getting a country's currency symbol from the country's code
     * e.g USA - USD
     */

    private fun getSymbol(countryCode: String?): String? {
        val availableLocales = Locale.getAvailableLocales()
        for (i in availableLocales.indices) {
            if (availableLocales[i].country == countryCode
            ) return Currency.getInstance(availableLocales[i]).currencyCode
        }
        return ""
    }


    /**
     * A method for getting a country's code from the country name
     * e.g Nigeria - NG
     */

    private fun getCountryCode(countryName: String) = Locale.getISOCountries().find { Locale("", it).displayCountry == countryName }


    /**
     * A method for getting all countries in the world - about 256 or so
     */

    private fun getAllCountries(): ArrayList<String> {

        val locales = Locale.getAvailableLocales()
        val countries = ArrayList<String>()
        for (locale in locales) {
            val country = locale.displayCountry
            if (country.trim { it <= ' ' }.isNotEmpty() && !countries.contains(country)) {
                countries.add(country)
            }
        }
        countries.sort()

        return countries
    }

    /**
     * A method for handling click events in the UI
     */

    private fun setUpClickListener(){

        binding.btnConvert.setOnClickListener {

            val numberToConvert = binding.etFirstCurrency.text.toString()

            if(numberToConvert.isEmpty() || numberToConvert == "0"){
                Snackbar.make(binding.mainLayout,"Input a value in the first text field, result will be shown in the second text field", Snackbar.LENGTH_LONG)
                    .withColor(ContextCompat.getColor(context as Activity, R.color.dark_red))
                    .setTextColor(ContextCompat.getColor(context as Activity, R.color.white))
                    .show()
            }

            //check if internet is available
            else if (!Utility.isNetworkAvailable(context as Activity)){
                Snackbar.make(binding.mainLayout,"You are not connected to the internet", Snackbar.LENGTH_LONG)
                    .withColor(ContextCompat.getColor(context as Activity, R.color.dark_red))
                    .setTextColor(ContextCompat.getColor(context as Activity, R.color.white))
                    .show()
            }

            //carry on and convert the value
            else{
                doConversion()
            }
        }

    }

    /**
     * A method that does the conversion by communicating with the API - fixer.io based on the data inputed
     * Uses viewModel and flows
     */

    private fun doConversion(){
        Utility.hideKeyboard(context as Activity)
        binding.prgLoading.visibility = View.VISIBLE
        binding.btnConvert.visibility = View.GONE

        val apiKey = EndPoints.API_KEY
        val from = selectedItem1.toString()
        val to = selectedItem2.toString()
        val amount = binding.etFirstCurrency.text.toString().toDouble()

        viewModel.getConvertedData(apiKey, from, to, amount)
    }

    /**
     * Using coroutines flow, changes are observed and responses gotten from the API
     *
     */

    @SuppressLint("SetTextI18n")
    private fun subscribeToLiveData() {
        viewModel.data.observe(this, {result ->
            when(result.status){
                Resource.Status.SUCCESS -> {
                    if (result.data?.status == "success"){
                        val map: Map<String, Rates>
                        map = result.data.rates
                        map.keys.forEach {
                            val rateForAmount = map[it]?.rate_for_amount
                            viewModel.convertedRate.value = rateForAmount
                            val formattedString = String.format("%,.2f", viewModel.convertedRate.value)
                            binding.etSecondCurrency.setText(formattedString)

                        }
                        binding.prgLoading.visibility = View.GONE
                        binding.btnConvert.visibility = View.VISIBLE
                    }
                    else if(result.data?.status == "fail"){
                        val layout = binding.mainLayout
                        Snackbar.make(layout,"Ooops! something went wrong, Try again", Snackbar.LENGTH_LONG)
                            .withColor(ContextCompat.getColor(context as Activity, R.color.dark_red))
                            .setTextColor(ContextCompat.getColor(context as Activity, R.color.white))
                            .show()
                        binding.prgLoading.visibility = View.GONE
                        binding.btnConvert.visibility = View.VISIBLE
                    }
                }
                Resource.Status.ERROR -> {
                    val layout = binding.mainLayout
                    Snackbar.make(layout,  "Oopps! Something went wrong, Try again", Snackbar.LENGTH_LONG)
                        .withColor(ContextCompat.getColor(context as Activity, R.color.dark_red))
                        .setTextColor(ContextCompat.getColor(context as Activity, R.color.white))
                        .show()
                    binding.prgLoading.visibility = View.GONE
                    binding.btnConvert.visibility = View.VISIBLE
                }
                Resource.Status.LOADING -> {
                    binding.prgLoading.visibility = View.VISIBLE
                    binding.btnConvert.visibility = View.GONE
                }
            }
        })
    }

    /**
     * Method for changing the background color of snackBars
     */

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }
}