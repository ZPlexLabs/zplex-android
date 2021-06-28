package zechs.zplex.adapter;

public class CardItem {

    String Name, Type, posterURL;

    public CardItem(String name, String type, String posterURL) {
        Name = name;
        Type = type;
        this.posterURL = posterURL;
    }

    public String getName() {
        return Name;
    }

    public String getType() {
        return Type;
    }

    public String getPosterURL() {
        return posterURL;
    }

}
