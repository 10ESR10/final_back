package com.example.demo.controller;

import com.example.demo.dto.CartDto;
import com.example.demo.dto.CartItemDto;
import com.example.demo.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public @ResponseBody
    ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }


        Long cartItemId;
        String email = cartItemDto.getEmail();
        try {
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);

    }

    @GetMapping
    public @ResponseBody ResponseEntity<List<CartDto>> orderHist(@RequestParam String email) {
        List<CartDto> cartList = cartService.getCartList(email);
        return new ResponseEntity<List<CartDto>>(cartList, HttpStatus.OK);
    }
    @PostMapping(value = "/deleteItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId, String email) {

        if(!cartService.validateCartItem(cartItemId, email)) {
            return new ResponseEntity<String> ("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @PostMapping(value = "/updateItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId, @RequestParam("quantity")int quantity,@RequestParam("email") String email){

        if(quantity <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        } else if(!cartService.validateCartItem(cartItemId, email)){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, quantity);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
}