console.log("script file")
const toggleSidebar=()=>{
    if($(".sidebar").is(":visible")){
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");

    }else{
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");

    }
};

const search = () => {
    let query = $("#search-input").val();

    if (query === '') {
        $(".search-result").hide();
    } else {
        console.log("Search query:", query);

        // Sending request to server
        let url = `http://localhost:8080/search/${query}`;

        fetch(url)
            .then((response) => response.json())
            .then((data) => {
                console.log("Response from server:", data);

                let text = `<div class='list-group'>`;
                data.forEach((contact) => {
                    text += `<a href="/user/${contact.cID}/contact" class='list-group-item list-group-item-action'>${contact.name}</a>`;
                });
                text += `</div>`;
                $(".search-result").html(text);
                $(".search-result").show();
            })
            .catch((error) => {
                console.error("Error fetching data:", error);
            });
    }
};



// ************  payment gateway  ****************

// first rw=equest to server to creeate order


const paymentStart=()=>{

    console.log("payment started");
    let amount=$("#payment_field").val();
    console.log("amount "+amount);
    if(amount==''|| amount==null)
    {
        alert("amount is required");
        return;
    }

    //we will use ajax to send request to sever to create order

    $.ajax({

        url:'/user/create_order',
        data:JSON.stringify({amount:amount,info:'order_request'}),
        contentType:'application/json',
        type:'POST',
        dataType:'json',
        success:function(response){

            //invoked when success
            console.log(response);


        },
        error:function(error){
            //invoked when error
            console.log(error);
            alert("something went wrong !!!");

        }
    })
};