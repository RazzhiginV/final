package com.example.lostfoundthings.data


data class Post(
    val id: Int,
    val title: String,
    val description: String,
    val photo: String?,
    val address: String,
    val lat: Double,
    val lon: Double,
    val authorName: String,
    val authorPhoto: String?,
    val state: String
)

class PostRepository {
    fun getFakePosts(): List<Post> {
        return listOf(
            Post(1, "Ключи", "desc1", "https://ic.pics.livejournal.com/cheshirrrko/77399885/27916/27916_600.jpg", "", 0.0, 0.0, "First", "https://www.shutterstock.com/image-vector/vector-flat-illustration-grayscale-avatar-600nw-2610386729.jpg", "lost"),
            Post(2, "Студенческий", "desc2", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQz83dqrz5C-MqWAsxBE720YBUhVsKR6uNzMA&s", "", 0.0, 0.0, "Second", "https://www.shutterstock.com/image-vector/vector-flat-illustration-grayscale-avatar-600nw-2610386729.jpg", "lost"),
            Post(3, "Телефон", "desc3", "https://bloknot-volgodonsk.ru/thumb/850x0xcut/upload/iblock/32e/fctmr062pmicha1ollrslnaglzl26pbt/Nashedshemu-na-lavochke-telefon-volgodontsu-grozit-do-pyati-let-lisheniya-svobody.jpg", "", 0.0, 0.0, "Third", "", "found"),
            Post(4, "item4", "desc4", "", "", 0.0, 0.0, "Fourth", "https://www.shutterstock.com/image-vector/vector-flat-illustration-grayscale-avatar-600nw-2610386729.jpg", "lost")
        )
    }
}