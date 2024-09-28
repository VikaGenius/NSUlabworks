using Hackathon.Hackathon;

namespace Hackathon
{
    public class WishList
    {
        private Participant owner;

        private List<Participant> priorityList;
        public List<Participant> PriorityList { get => priorityList; set => priorityList = value; }
        public Participant Owner { get => owner; set => owner = value; }

        public WishList(Participant owner, List<Participant> participants)
        {
            Owner = owner;
            PriorityList = participants;
        }
    }
}


//     public class WishListsStorage
//     {
//         private Dictionary<string, WishList> wishes;
//         public Dictionary<string, WishList> Wishes { get => wishes; set => wishes = value; }

//         public int indexInWishes(string name, Participant target)
//         {
//             return Wishes.ContainsKey(name) ? wishes[name].PriorityList.IndexOf(target) : -1;

//         }

//         public void Add(string name, WishList wishList)
//         {
//             wishes.Add(name, wishList);
//         }

//         public WishList Get(string name) {

//         }
//     }
// }