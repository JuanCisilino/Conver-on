package com.frost.converon.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.frost.converon.databinding.FragmentCalculatorBinding

class CalculatorFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this).get(CalculatorViewModel::class.java) }
    private var _binding: FragmentCalculatorBinding? = null
    private var isNewOp = true
    private var oldNumber = ""
    private var op = "+"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        setNumberClick()
        setSimbolsClick()
        return binding.root
    }

    private fun setSimbolsClick() {
        binding.btAC.setOnClickListener {
            binding.editText.setText("0")
            isNewOp = true
        }
        binding.btPlusMinus.setOnClickListener { setBuClick("+-") }
        binding.btDivide.setOnClickListener { setBuClick("/") }
        binding.btMultiply.setOnClickListener { setBuClick("*") }
        binding.btMinus.setOnClickListener { setBuClick("-") }
        binding.btPlus.setOnClickListener { setBuClick("+") }
        binding.btEqual.setOnClickListener { setEqualEvent() }
        binding.btPercentge.setOnClickListener { setPercentageEvent() }
    }

    private fun setPercentageEvent() {
        val no = binding.editText.text.toString().toDouble()/100
        binding.editText.setText(no.toString())
        isNewOp = true
    }

    private fun setEqualEvent(){
        val newNumber = binding.editText.text.toString()
        val result = when (op){
            "/" -> oldNumber.toDouble() / newNumber.toDouble()
            "*" -> oldNumber.toDouble() * newNumber.toDouble()
            "+" -> oldNumber.toDouble() + newNumber.toDouble()
            "-" -> oldNumber.toDouble() - newNumber.toDouble()
            else -> 0.0
        }
        binding.editText.setText(result.toString())
        oldNumber = result.toString()
    }

    private fun setNumberClick(){
        binding.bt0.setOnClickListener { setBuClick("0") }
        binding.bt1.setOnClickListener { setBuClick("1") }
        binding.bt2.setOnClickListener { setBuClick("2") }
        binding.bt3.setOnClickListener { setBuClick("3") }
        binding.bt4.setOnClickListener { setBuClick("4") }
        binding.bt5.setOnClickListener { setBuClick("5") }
        binding.bt6.setOnClickListener { setBuClick("6") }
        binding.bt7.setOnClickListener { setBuClick("7") }
        binding.bt8.setOnClickListener { setBuClick("8") }
        binding.bt9.setOnClickListener { setBuClick("9") }
        binding.btDot.setOnClickListener { setBuClick(".") }
    }

    private fun setBuClick(buClick: String){
        if (isNewOp) binding.editText.setText("")
        isNewOp = false
        var result = binding.editText.text.toString()
        when (buClick){
            "+-" -> result = "-$result"
            "/" -> setFactor("/", result)
            "*" -> setFactor("*", result)
            "+" -> setFactor("+", result)
            "-" -> setFactor("-", result)
            else -> result += buClick
        }
        binding.editText.setText(result)
    }

    private fun setFactor(factor: String, result:String){
        isNewOp = true
        oldNumber = result
        op = factor
        binding.editText.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}