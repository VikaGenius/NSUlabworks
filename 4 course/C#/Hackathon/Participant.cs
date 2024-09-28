namespace Hackathon
{
    namespace Hackathon
    {
        public class Participant 
        {
            public int Id { get; set; }
            public string? Name { get; set; }
            public bool IsFree { get; set; } = true;
            public string Type { get; set; }
            public Participant (int id, string name, string type) {
                Id = id;
                Name = name;
                Type = type;
            }
            public WishList GenerateWishList(List<Participant> participants)
            {
                return new WishList(this, Utils.ShuffleList(participants));
            }


        }
    }
}