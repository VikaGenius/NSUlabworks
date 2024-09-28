namespace Hackathon
{
    public class SampleStrategy
    {
        public static List<Team> FormTeams(List<WishList> wishLists) //мапу
        {
            var teams = new List<Team>();
            while (wishLists.Any(j => j.Owner.IsFree))
            {
                foreach (var wishList in wishLists.Where(p => p.Owner.IsFree))
                {
                    var owner = wishList.Owner;
                    foreach (var preferred in wishList.PriorityList)
                    {
                        if (preferred.IsFree)
                        {
                            if (preferred.Type.Equals("Junior")) teams.Add(new Team(preferred, owner));
                            else teams.Add(new Team(owner, preferred));
                            preferred.IsFree = false;
                            wishList.Owner.IsFree = false;
                            break;
                        }
                    }
                }
            }

            return teams;
        }
    }
}